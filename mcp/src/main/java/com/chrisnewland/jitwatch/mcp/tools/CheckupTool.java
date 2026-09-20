package com.chrisnewland.jitwatch.mcp.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.mcp.answer.Answer;
import com.chrisnewland.jitwatch.mcp.findings.Finding;
import com.chrisnewland.jitwatch.mcp.findings.FindingsEngine;
import com.chrisnewland.jitwatch.mcp.session.Capabilities;
import com.chrisnewland.jitwatch.mcp.session.Session;

public final class CheckupTool implements Tool {

	private final ToolContext context;

	public CheckupTool(ToolContext context) {
		this.context = context;
	}

	@Override
	public String name() {
		return "checkup";
	}

	@Override
	public String title() {
		return "What is worth looking at in this log";
	}

	@Override
	public String description() {
		return "Rank what the JIT compiler did into a short list of findings in plain language, worst first, each with a place to look and something to try. This is the place to start when someone says something is slow. Reports honestly when nothing in the log explains a slowdown.";
	}

	@Override
	public java.util.Map<String, Object> schema() {
		return Schemas.object()
			.reqStr("session_id", "Session id from load_log")
			.str("package_prefix", "Only look at classes whose fully qualified name starts with this")
			.integer("limit", "How many findings to return. Default 10.")
			.build();
	}

	@Override
	public Answer call(ToolArguments arguments) throws Exception {
		return checkup(arguments.string("session_id"), arguments.string("package_prefix"), arguments.integer("limit"));
	}

	public Answer checkup(String sessionId, String packagePrefix, Integer limit) throws Exception {
		Session session = context.requireSession(sessionId);

		int effectiveLimit = context.budget().clampLimit(limit == null ? 10 : limit);

		List<Finding> findings = FindingsEngine.checkup(session, packagePrefix, effectiveLimit);

		Answer answer = new Answer();

		List<Map<String, Object>> rendered = new ArrayList<>();

		for (Finding finding : findings) {
			rendered.add(finding.toMap());
		}

		answer.put("findings", rendered);
		answer.put("scope", packagePrefix == null || packagePrefix.isBlank() ? "whole log" : packagePrefix);

		boolean blind = !session.getCapabilities().has(Capabilities.INLINING);

		if (findings.isEmpty() && blind) {
			// An empty list here would read as a clean bill of health when the truth is
			// that most of the evidence was never written to the log.
			answer.put("verdict", "capability_unavailable");
			answer.put("can_answer", session.getCapabilities().toEvidence());
			answer.plain("This log cannot be checked for inlining problems, so the empty result below is not a "
					+ "clean bill of health. " + session.getCapabilities().warning()
					+ " Deoptimisation evidence does survive: call get_deopts.");
		}
		else if (findings.isEmpty()) {
			answer.plain("Nothing in this log points at a JIT problem"
					+ (packagePrefix == null || packagePrefix.isBlank() ? "" : " in " + packagePrefix)
					+ ". If something is slow, the cause is more likely garbage collection, I/O, lock contention or an "
					+ "algorithm, and a profiler will tell you more than a compilation log can.");
			answer.put("verdict", "no_jit_explanation");
		}
		else if (FindingsEngine.nothingActionable(findings)) {
			answer.plain("Found " + findings.size()
					+ " things worth knowing, but none of them are hot enough to explain a slowdown on their own. "
					+ "If something is slow, check garbage collection, I/O and lock contention too.");
			answer.put("verdict", "informational_only");
		}
		else {
			Finding top = findings.get(0);

			answer.plain("Found " + findings.size() + " findings. The most significant: " + top.plain
					+ (top.fix == null ? "" : " " + top.fix));
			answer.put("verdict", "findings_could_explain_a_slowdown");
		}

		if (blind && !findings.isEmpty()) {
			answer.note("Inlining evidence is missing from this log, so these findings are only the part "
					+ "that survived. " + session.getCapabilities().warning());
		}

		answer.note("A compilation log records what the compiler did, not how long anything took. "
				+ "Use these findings to explain measurements, not as measurements.");

		return answer;
	}

}
