package com.chrisnewland.jitwatch.mcp.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.model.Compilation;
import com.chrisnewland.jitwatch.model.IMetaMember;

import com.chrisnewland.jitwatch.mcp.answer.Answer;
import com.chrisnewland.jitwatch.mcp.findings.FindingsEngine;
import com.chrisnewland.jitwatch.mcp.query.Decision;
import com.chrisnewland.jitwatch.mcp.query.QueryService;
import com.chrisnewland.jitwatch.mcp.session.Session;

public final class ExplainMethodTool implements Tool {

	private final ToolContext context;

	public ExplainMethodTool(ToolContext context) {
		this.context = context;
	}

	@Override
	public String name() {
		return "explain_method";
	}

	@Override
	public String title() {
		return "The full story of one method";
	}

	@Override
	public String description() {
		return "How often the method was compiled and by which compiler, how large it is in bytecode and in machine code, what it managed to inline and what it did not, where the compiler planted speculations, and whether its compiled code was ever thrown away.";
	}

	@Override
	public java.util.Map<String, Object> schema() {
		return Schemas.object()
			.reqStr("session_id", "Session id from load_log")
			.reqStr("signature", "Method name or signature, for example (JitDemo.big) or a fully qualified signature")
			.build();
	}

	@Override
	public Answer call(ToolArguments arguments) throws Exception {
		return explainMethod(arguments.string("session_id"), arguments.string("signature"));
	}

	public Answer explainMethod(String sessionId, String signature) throws Exception {
		Session session = context.requireSession(sessionId);

		IMetaMember member = context.requireMember(session, signature);

		Answer answer = new Answer();

		Map<String, Object> summary = QueryService.memberSummary(member);
		answer.put("method", summary);
		answer.put("source", QueryService.sourceLocation(session, member));

		List<Map<String, Object>> compilations = new ArrayList<>();

		for (Compilation compilation : member.getCompilations()) {
			compilations.add(QueryService.compilationSummary(compilation));
		}

		answer.put("compilations", context.budget()
			.capList(compilations, context.budget().maxListItems(), "compilations", answer.getEvidence()));

		List<Decision> decisions = QueryService.decisionsIn(session, member, -1);

		// One row per call site, carrying the compiler's final word on it
		Map<Integer, Decision> perSite = new LinkedHashMap<>();

		for (Decision decision : decisions) {
			Decision existing = perSite.get(decision.bci);

			if (existing == null || decision.tier >= existing.tier) {
				perSite.put(decision.bci, decision);
			}
		}

		List<Map<String, Object>> callSites = new ArrayList<>();

		int inlined = 0;
		int notInlined = 0;
		int intrinsics = 0;

		for (Decision decision : perSite.values()) {
			callSites.add(decision.toMap());

			switch (decision.outcome) {
				case Decision.INLINED:
					inlined++;
					break;
				case Decision.INTRINSIC:
					intrinsics++;
					break;
				default:
					notInlined++;
					break;
			}
		}

		answer.put("call_sites", context.budget()
			.capList(callSites, context.budget().maxListItems(), "call_sites", answer.getEvidence()));

		List<Map<String, Object>> deopts = QueryService.deoptsIn(session, member);

		if (!deopts.isEmpty()) {
			answer.put("speculation_points", context.budget()
				.capList(deopts, context.budget().maxListItems(), "speculation_points", answer.getEvidence()));
		}

		int decompiles = QueryService.decompileCount(member);

		if (decompiles > 0) {
			answer.put("times_thrown_away", decompiles);
		}

		Integer bytecodeSize = QueryService.bytecodeSize(member);

		Compilation best = QueryService.bestCompilation(member);

		StringBuilder plain = new StringBuilder();

		plain.append(FindingsEngine.shortName(QueryService.signatureOf(member)));

		if (bytecodeSize != null) {
			plain.append(" is ").append(bytecodeSize).append(" bytes of bytecode");
		}

		plain.append(" and was compiled ")
			.append(member.getCompilations().size())
			.append(" time")
			.append(member.getCompilations().size() == 1 ? "" : "s");

		if (best != null) {
			plain.append(", most recently by ")
				.append(best.getCompiler())
				.append(" into ")
				.append(best.getNativeSize())
				.append(" bytes of machine code");
		}

		plain.append(". Inside it, ")
			.append(inlined)
			.append(" call")
			.append(inlined == 1 ? " was" : "s were")
			.append(" inlined and ")
			.append(notInlined)
			.append(" not");

		if (intrinsics > 0) {
			plain.append("; ")
				.append(intrinsics)
				.append(" call")
				.append(intrinsics == 1 ? " was" : "s were")
				.append(" replaced by a built-in machine instruction");
		}

		plain.append('.');

		if (decompiles > 0) {
			plain.append(" Its compiled code was thrown away ")
				.append(decompiles)
				.append(decompiles == 1 ? " time" : " times")
				.append(" because a speculation turned out wrong.");
		}

		answer.plain(plain.toString());

		if (QueryService.lineTableOf(session, member) == null) {
			answer.note("No line numbers are available for this method. Mount the class files, compiled with -g, "
					+ "to get source line numbers in the answers.");
		}

		return answer;
	}

}
