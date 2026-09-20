package com.chrisnewland.jitwatch.mcp.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.toplist.CompileTimeTopListVisitable;
import com.chrisnewland.jitwatch.toplist.HotThrowTopListVisitable;
import com.chrisnewland.jitwatch.toplist.InliningFailReasonTopListVisitable;
import com.chrisnewland.jitwatch.toplist.ITopListScore;
import com.chrisnewland.jitwatch.toplist.ITopListVisitable;
import com.chrisnewland.jitwatch.toplist.MostUsedIntrinsicsTopListVisitable;
import com.chrisnewland.jitwatch.toplist.NativeMethodSizeTopListVisitable;

import com.chrisnewland.jitwatch.mcp.answer.Answer;
import com.chrisnewland.jitwatch.mcp.query.QueryService;
import com.chrisnewland.jitwatch.mcp.session.Capabilities;
import com.chrisnewland.jitwatch.mcp.session.LogScan;
import com.chrisnewland.jitwatch.mcp.session.Session;

public final class TopListTool implements Tool {

	private final ToolContext context;

	public TopListTool(ToolContext context) {
		this.context = context;
	}

	@Override
	public String name() {
		return "top_list";
	}

	@Override
	public String title() {
		return "Rank the whole run";
	}

	@Override
	public String description() {
		return "League tables over the whole log: the largest compiled methods, the slowest compilations, the most common reasons for not inlining, the most used built-in machine-code implementations, and exceptions thrown on hot paths.";
	}

	@Override
	public java.util.Map<String, Object> schema() {
		return Schemas.object()
			.reqStr("session_id", "Session id from load_log")
			.enumStr("kind", "Which ranking", true,
					java.util.List.of("largest_native", "longest_compile", "inline_fail_reasons", "most_intrinsics",
							"hot_throws", "deopt_reasons"))
			.integer("limit", "How many rows to return")
			.build();
	}

	@Override
	public Answer call(ToolArguments arguments) throws Exception {
		return topList(arguments.string("session_id"), arguments.string("kind"), arguments.integer("limit"));
	}

	public Answer topList(String sessionId, String kind, Integer limit) throws Exception {
		Session session = context.requireSession(sessionId);

		int effectiveLimit = context.budget().clampLimit(limit);

		String requested = kind == null ? "" : kind.toLowerCase(Locale.ROOT);

		if ("deopt_reasons".equals(requested)) {
			return deoptReasons(session, effectiveLimit);
		}

		ITopListVisitable visitable;

		String plainKind;

		switch (requested) {
			case "largest_native":
				visitable = new NativeMethodSizeTopListVisitable(session.getModel(), true);
				plainKind = "the methods with the most compiled machine code";
				break;

			case "longest_compile":
				session.getCapabilities().require(Capabilities.COMPILE_TIMES);
				visitable = new CompileTimeTopListVisitable(session.getModel(), true);
				plainKind = "the methods the compiler spent longest on";
				break;

			case "inline_fail_reasons":
				session.getCapabilities().require(Capabilities.INLINING);
				visitable = new InliningFailReasonTopListVisitable(session.getModel(), true);
				plainKind = "the reasons the compiler most often gave for not inlining";
				break;

			case "most_intrinsics":
				session.getCapabilities().require(Capabilities.INTRINSICS);
				visitable = new MostUsedIntrinsicsTopListVisitable(session.getModel(), true);
				plainKind = "the built-in machine-code implementations used most";
				break;

			case "hot_throws":
				session.getCapabilities().require(Capabilities.HOT_THROWS);
				visitable = new HotThrowTopListVisitable(session.getModel(), true);
				plainKind = "exceptions thrown often enough for the compiler to notice";
				break;

			default:
				throw new IllegalArgumentException("Unknown kind: " + kind
						+ ". Use largest_native, longest_compile, inline_fail_reasons, most_intrinsics, "
						+ "hot_throws or deopt_reasons.");
		}

		List<ITopListScore> scores = visitable.buildTopList();

		List<Map<String, Object>> rows = new ArrayList<>();

		int zeros = 0;

		for (ITopListScore score : scores) {
			// A score of zero means the attribute behind it was never written to the log.
			// Returning thousands of those as a "ranking" is worse than returning none.
			if (score.getScore() <= 0) {
				zeros++;
				continue;
			}

			Map<String, Object> row = new LinkedHashMap<>();

			Object key = score.getKey();

			if (key instanceof IMetaMember member) {
				row.put("signature", QueryService.signatureOf(member));
			}
			else {
				row.put("key", context.budget().capString(String.valueOf(key)));
			}

			row.put("value", score.getScore());
			rows.add(row);
		}

		Answer answer = new Answer();

		answer.put("kind", kind);
		answer.put("rows", context.budget().capList(rows, effectiveLimit, "rows", answer.getEvidence()));

		if (rows.isEmpty() && zeros > 0) {
			answer.put("verdict", "no_values_recorded");
			answer.plain("This log records no usable value for " + plainKind + ": all " + zeros
					+ " candidates came back as zero, which means the attribute behind them was never written.");
		}
		else if (rows.isEmpty()) {
			answer.plain("This log has nothing to rank for " + plainKind + ".");
		}
		else {
			answer.plain("Ranked " + plainKind + ", highest first.");

			if (zeros > 0) {
				answer.note(zeros + " candidates were left out because the log records no value for them.");
			}
		}

		return answer;
	}

	/**
	 * Ranks the reasons the JVM abandoned optimised code. The parser drops top level
	 * uncommon_trap tags, so this reads the scan taken when the log was loaded; it is the
	 * only deoptimisation evidence that survives a log without parse trees.
	 */
	private Answer deoptReasons(Session session, int limit) {
		Map<String, Integer> byReason = new LinkedHashMap<>();
		Map<String, Integer> byAction = new LinkedHashMap<>();

		for (LogScan.Trap trap : session.getScan().traps()) {
			if (trap.reason() != null) {
				byReason.merge(trap.reason(), 1, Integer::sum);
			}

			if (trap.action() != null) {
				byAction.merge(trap.action(), 1, Integer::sum);
			}
		}

		List<Map<String, Object>> rows = new ArrayList<>();

		byReason.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).forEach(e -> {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("key", e.getKey());
			row.put("value", e.getValue());
			row.put("meaning", MEANINGS.getOrDefault(e.getKey(), "See the HotSpot deoptimisation reasons."));
			rows.add(row);
		});

		Answer answer = new Answer();
		answer.put("kind", "deopt_reasons");
		answer.put("total_traps", session.getScan().traps().size());
		answer.put("actions", byAction);
		answer.put("rows", context.budget().capList(rows, limit, "rows", answer.getEvidence()));

		if (rows.isEmpty()) {
			answer.plain("The JVM never abandoned optimised code in this run.");
		}
		else {
			Map.Entry<String, Integer> top = byReason.entrySet()
				.stream()
				.max(Map.Entry.comparingByValue())
				.orElseThrow();

			answer.plain("The JVM threw away optimised code " + session.getScan().traps().size()
					+ " times. The commonest cause was " + top.getKey() + " (" + top.getValue() + "): "
					+ MEANINGS.getOrDefault(top.getKey(), "see the HotSpot deoptimisation reasons") + ".");
		}

		return answer;
	}

	private static final Map<String, String> MEANINGS = meanings();

	private static Map<String, String> meanings() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("unstable_if", "a branch the compiler had never seen taken was taken");
		map.put("class_check", "a call site saw a type it had not been compiled for");
		map.put("bimorphic_or_optimized_type_check", "a third receiver type appeared where two were assumed");
		map.put("null_check", "a value assumed non-null was null");
		map.put("range_check", "an index fell outside the range the compiler proved");
		map.put("array_check", "an array store saw an unexpected element type");
		map.put("div0_check", "a division by zero the compiler had ruled out");
		map.put("loop_limit_check", "a loop bound broke the assumption the compiler optimised for");
		map.put("profile_predicate", "a guard derived from the profile stopped holding");
		map.put("predicate", "a speculative guard stopped holding");
		map.put("unloaded", "a class referenced by the compiled code was still not loaded");
		map.put("uninitialized", "a class was not yet initialised when the compiled code ran");
		map.put("speculate_class_check", "a speculated receiver type turned out wrong");
		map.put("speculate_null_assert", "a value speculated non-null was null");
		map.put("unstable_fused_if", "a fused branch the compiler had never seen taken was taken");
		map.put("intrinsic_or_type_checked_inlining", "an intrinsic or type-checked inline guard failed");
		map.put("constraint", "a constraint the compiler relied on was violated");
		return map;
	}

}
