package com.chrisnewland.jitwatch.mcp.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.model.IMetaMember;

import com.chrisnewland.jitwatch.mcp.answer.Answer;
import com.chrisnewland.jitwatch.mcp.findings.FindingsEngine;
import com.chrisnewland.jitwatch.mcp.query.Decision;
import com.chrisnewland.jitwatch.mcp.query.QueryService;
import com.chrisnewland.jitwatch.mcp.session.Session;

public final class ExplainCallSiteTool implements Tool {

	private final ToolContext context;

	public ExplainCallSiteTool(ToolContext context) {
		this.context = context;
	}

	@Override
	public String name() {
		return "explain_call_site";
	}

	@Override
	public String title() {
		return "Why one call was or was not inlined";
	}

	@Override
	public String description() {
		return "For a call at a source line or bytecode index, report what each compiler decided, the reason it gave, and the flag and limit that applied, so the answer carries the number rather than a recollection of it.";
	}

	@Override
	public java.util.Map<String, Object> schema() {
		return Schemas.object()
			.reqStr("session_id", "Session id from load_log")
			.reqStr("signature", "The method containing the call")
			.integer("line", "Source line number of the call. Needs mounted classes compiled with -g.")
			.integer("bci", "Bytecode index of the call, as an alternative to line")
			.build();
	}

	@Override
	public Answer call(ToolArguments arguments) throws Exception {
		return explainCallSite(arguments.string("session_id"), arguments.string("signature"), arguments.integer("line"),
				arguments.integer("bci"));
	}

	public Answer explainCallSite(String sessionId, String signature, Integer line, Integer bci) throws Exception {
		Session session = context.requireSession(sessionId);

		IMetaMember member = context.requireMember(session, signature);

		if (line == null && bci == null) {
			throw new IllegalArgumentException("Give either a source line or a bytecode index (bci)");
		}

		List<Integer> targets = new ArrayList<>();

		if (bci != null) {
			targets.add(bci);
		}
		else {
			targets.addAll(QueryService.bciRangeForLine(session, member, line));
		}

		Answer answer = new Answer();

		answer.put("method", QueryService.signatureOf(member));

		if (line != null) {
			answer.put("line", line);
		}

		if (targets.isEmpty()) {
			answer.plain("No bytecode maps to line " + line + " in " + FindingsEngine.shortName(signature)
					+ ". Either that line has no code, or the class files were compiled without debug information "
					+ "and no line table is available. Try passing a bci instead.");
			answer.put("decisions", List.of());
			return answer;
		}

		List<Map<String, Object>> rows = new ArrayList<>();

		List<Decision> all = new ArrayList<>();

		for (int target : targets) {
			for (Decision decision : QueryService.decisionsIn(session, member, target)) {
				all.add(decision);
				rows.add(decision.toMap());
			}
		}

		answer.put("bytecode_indices", targets);
		answer.put("decisions",
				context.budget().capList(rows, context.budget().maxListItems(), "decisions", answer.getEvidence()));

		if (all.isEmpty()) {
			answer.plain("The compiler made no inlining decision at that position. Either there is no call there, or "
					+ "the code around it was never compiled.");
			return answer;
		}

		// Explain the final word: the highest tier that considered this site
		all.sort(Comparator.comparingInt(d -> d.tier));

		Decision last = all.get(all.size() - 1);

		StringBuilder plain = new StringBuilder();

		if (Decision.INLINED.equals(last.outcome)) {
			plain.append("This call was inlined by ")
				.append(last.compiler)
				.append(", so at run time there is no call here at all: the callee's code is part of the caller.");
		}
		else if (Decision.INTRINSIC.equals(last.outcome)) {
			plain.append("The compiler replaced this call with a built-in machine-code implementation.");
		}
		else {
			plain.append("This call was NOT inlined by ").append(last.compiler).append(". ").append(last.plain);

			if (last.actual != null && last.limit != null) {
				plain.append(" The callee is ")
					.append(last.actual)
					.append(" bytes against a limit of ")
					.append(last.limit)
					.append(" (")
					.append(last.flag)
					.append(").");
			}

			plain.append(" Every execution pays for a real method call.");
		}

		if (all.size() > 1) {
			plain.append(" Earlier compilations of the same method reached the same call site; each is listed with the "
					+ "wording that compiler used.");
		}

		answer.plain(plain.toString());

		return answer;
	}

}
