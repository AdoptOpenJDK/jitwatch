package com.chrisnewland.jitwatch.mcp.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.model.Compilation;
import com.chrisnewland.jitwatch.model.IMetaMember;

import com.chrisnewland.jitwatch.mcp.answer.Answer;
import com.chrisnewland.jitwatch.mcp.findings.FindingsEngine;
import com.chrisnewland.jitwatch.mcp.query.QueryService;
import com.chrisnewland.jitwatch.mcp.session.LogScan;
import com.chrisnewland.jitwatch.mcp.session.Session;

public final class DeoptsTool implements Tool {

	private final ToolContext context;

	public DeoptsTool(ToolContext context) {
		this.context = context;
	}

	@Override
	public String name() {
		return "get_deopts";
	}

	@Override
	public String title() {
		return "Methods whose compiled code was thrown away";
	}

	@Override
	public String description() {
		return "Deoptimisation: the JVM optimised a method for behaviour it saw during warm-up, the behaviour changed, and the optimised code had to be discarded and rebuilt. A common cause of a latency spike after a configuration change or a traffic shift.";
	}

	@Override
	public java.util.Map<String, Object> schema() {
		return Schemas.object()
			.reqStr("session_id", "Session id from load_log")
			.str("signature", "Restrict to one method. Omit for the whole log.")
			.integer("limit", "How many methods to return")
			.build();
	}

	@Override
	public Answer call(ToolArguments arguments) throws Exception {
		return deopts(arguments.string("session_id"), arguments.string("signature"), arguments.integer("limit"));
	}

	public Answer deopts(String sessionId, String signature, Integer limit) throws Exception {
		Session session = context.requireSession(sessionId);

		int effectiveLimit = context.budget().clampLimit(limit);

		List<IMetaMember> candidates;

		if (signature == null || signature.isBlank()) {
			candidates = FindingsEngine.compiledMembers(session, null);
		}
		else {
			candidates = List.of(context.requireMember(session, signature));
		}

		List<Map<String, Object>> rows = new ArrayList<>();

		for (IMetaMember member : candidates) {
			int decompiles = QueryService.decompileCount(member);

			if (decompiles <= 0) {
				continue;
			}

			Map<String, Object> row = new LinkedHashMap<>();
			row.put("signature", QueryService.signatureOf(member));
			row.put("times_thrown_away", decompiles);
			row.put("compilations", member.getCompilations().size());
			row.put("invocations", QueryService.heatOf(member));

			List<Map<String, Object>> traps = QueryService.deoptsIn(session, member);

			if (!traps.isEmpty()) {
				row.put("speculation_points", context.budget().capList(traps, 5, "speculation_points", null));
			}

			// The parse trees carry the bci of each trap, but they are absent from a
			// truncated or C1-only log. The top level uncommon_trap tags survive both, so
			// the reason is still recoverable through the compile ids of this member.
			Map<String, Integer> reasons = reasonsFor(session, member);

			if (!reasons.isEmpty()) {
				row.put("reasons", reasons);
			}

			rows.add(row);
		}

		rows.sort(Comparator.comparingLong((Map<String, Object> r) -> ((Number) r.get("times_thrown_away")).longValue())
			.reversed());

		Answer answer = new Answer();

		answer.put("deoptimised_methods",
				context.budget().capList(rows, effectiveLimit, "deoptimised_methods", answer.getEvidence()));

		int scanned = session.getScan().traps().size();

		if (scanned > 0) {
			answer.put("uncommon_traps_in_log", scanned);
		}

		if (rows.isEmpty() && scanned > 0) {
			answer.plain("No method reports a discarded compilation, but the log records " + scanned
					+ " uncommon traps. Call top_list with kind=deopt_reasons to see why the JVM abandoned "
					+ "optimised code.");
		}
		else if (rows.isEmpty()) {
			answer.plain("No method in this log had its compiled code thrown away. The JVM's assumptions held for the "
					+ "whole run.");
		}
		else {
			answer.plain(rows.size() + " method" + (rows.size() == 1 ? "" : "s")
					+ " had compiled code thrown away and had to be recompiled. That happens when the JVM optimises for "
					+ "behaviour it saw during warm-up and the behaviour later changes.");
		}

		return answer;
	}

	private static Map<String, Integer> reasonsFor(Session session, IMetaMember member) {
		Set<String> compileIds = new HashSet<>();

		for (Compilation compilation : member.getCompilations()) {
			if (compilation.getCompileID() != null) {
				compileIds.add(compilation.getCompileID());
			}
		}

		Map<String, Integer> reasons = new LinkedHashMap<>();

		for (LogScan.Trap trap : session.getScan().traps()) {
			if (trap.reason() != null && trap.compileId() != null && compileIds.contains(trap.compileId())) {
				reasons.merge(trap.reason(), 1, Integer::sum);
			}
		}

		return reasons;
	}

}
