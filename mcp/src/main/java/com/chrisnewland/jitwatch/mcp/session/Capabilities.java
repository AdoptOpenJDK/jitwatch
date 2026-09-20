package com.chrisnewland.jitwatch.mcp.session;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What this log can and cannot answer, decided once when it is loaded.
 *
 * Without this a tool cannot tell "the compiler recorded nothing" from "this log cannot
 * contain it", and both render as an empty list. Those are opposite answers: the first
 * says the JIT is fine and to look elsewhere, the second says the log is the wrong log
 * and to produce a better one. Reporting the second as the first is the worst answer the
 * server can give, so every tool that needs a missing feature refuses by name instead.
 */
public final class Capabilities {

	public static final String INLINING = "inlining";

	public static final String COMPILE_TIMES = "compile_times";

	public static final String INTRINSICS = "intrinsics";

	public static final String HOT_THROWS = "hot_throws";

	private final boolean parseTrees;

	private final boolean taskBodies;

	private final boolean c2;

	private final boolean closedCleanly;

	private final String tieredStopAtLevel;

	private final int unresolvedClasses;

	private final int compilationsInLog;

	private Capabilities(boolean parseTrees, boolean taskBodies, boolean c2, boolean closedCleanly,
			String tieredStopAtLevel, int unresolvedClasses, int compilationsInLog) {
		this.parseTrees = parseTrees;
		this.taskBodies = taskBodies;
		this.c2 = c2;
		this.closedCleanly = closedCleanly;
		this.tieredStopAtLevel = tieredStopAtLevel;
		this.unresolvedClasses = unresolvedClasses;
		this.compilationsInLog = compilationsInLog;
	}

	public static Capabilities of(LogScan scan, String vmArguments, SessionStore.ParseErrorTally tally) {
		boolean parseTrees = scan.count("parse") > 0;
		boolean taskBodies = scan.count("task_done") > 0;
		boolean c2 = scan.count("nmethod") > 0 && hasC2(scan);

		return new Capabilities(parseTrees, taskBodies, c2, scan.isClosedCleanly(), tieredStopAtLevel(vmArguments),
				tally == null ? 0 : tally.unresolvedClasses(), scan.count("nmethod"));
	}

	/**
	 * A class the parser could not load never enters the model, so its compilations are
	 * absent from every count and every tool. That is a far larger hole than the missing
	 * bytecode detail the caller is warned about, and it is silent, so it is reported as
	 * a share of the log rather than as a footnote.
	 */
	public int unresolvedClasses() {
		return unresolvedClasses;
	}

	public boolean hasSignificantUnresolved() {
		return unresolvedClasses > 0 && (compilationsInLog <= 0 || unresolvedClasses * 100L / compilationsInLog >= 2);
	}

	private String unresolvedWarning() {
		return "This session is incomplete: " + unresolvedClasses
				+ " compilations name classes the parser could not load, so they are missing from every "
				+ "count and every tool. Mount the dependency jars and the application classes with "
				+ "class_roots and load the log again.";
	}

	private static boolean hasC2(LogScan scan) {
		return scan.count("uncommon_trap") > 0 || scan.count("parse") > 0;
	}

	private static String tieredStopAtLevel(String vmArguments) {
		if (vmArguments == null) {
			return null;
		}

		int at = vmArguments.indexOf("-XX:TieredStopAtLevel=");

		if (at < 0) {
			return null;
		}

		int from = at + "-XX:TieredStopAtLevel=".length();
		int to = from;

		while (to < vmArguments.length() && Character.isDigit(vmArguments.charAt(to))) {
			to++;
		}

		return to > from ? vmArguments.substring(from, to) : null;
	}

	public boolean has(String feature) {
		switch (feature) {
			case INLINING:
			case INTRINSICS:
			case HOT_THROWS:
				return parseTrees;

			case COMPILE_TIMES:
				return taskBodies;

			default:
				return true;
		}
	}

	/** Throws with the reason and the fix when the log cannot support the feature. */
	public void require(String feature) {
		if (!has(feature)) {
			throw new IllegalArgumentException(explain(feature));
		}
	}

	private String explain(String feature) {
		return "This log cannot answer that: " + missing(feature) + " " + cause() + " " + remedy();
	}

	private static String missing(String feature) {
		switch (feature) {
			case INLINING:
				return "it records no inlining decisions.";

			case INTRINSICS:
				return "it records no intrinsic usage.";

			case HOT_THROWS:
				return "it records no hot throws.";

			case COMPILE_TIMES:
				return "it records no compilation timings.";

			default:
				return "the evidence is absent.";
		}
	}

	private String cause() {
		if (tieredStopAtLevel != null) {
			return "The run used -XX:TieredStopAtLevel=" + tieredStopAtLevel
					+ ", so C2 never ran and no <parse> trees were written.";
		}

		if (!closedCleanly) {
			return "The log has no closing tag, so the JVM was killed before the compiler threads "
					+ "flushed their <task> bodies, which is where that evidence lives.";
		}

		if (!c2) {
			return "No C2 compilation appears in the log.";
		}

		return "The <parse> trees that carry it are not present.";
	}

	private String remedy() {
		if (tieredStopAtLevel != null) {
			return "Re-run without -XX:TieredStopAtLevel (in IntelliJ, add the Spring run option "
					+ "'Disable launch optimization').";
		}

		if (!closedCleanly) {
			return "Re-run and stop the JVM gracefully so the log is flushed and closed.";
		}

		return "Re-run with -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation and let the program reach steady state.";
	}

	/**
	 * The one sentence load_log leads with when the log is crippled, or null when it is
	 * sound.
	 */
	public String warning() {
		if (hasSignificantUnresolved()) {
			return unresolvedWarning();
		}

		if (tieredStopAtLevel != null) {
			return "This run used -XX:TieredStopAtLevel=" + tieredStopAtLevel
					+ ", so C2 never ran. The log contains no inlining decisions and checkup cannot "
					+ "look for them. " + remedy();
		}

		if (!closedCleanly && !parseTrees) {
			return "The log has no closing tag and contains no <parse> trees: the JVM was killed before "
					+ "the compiler threads flushed them. Deoptimisation evidence survives, inlining does not. "
					+ remedy();
		}

		if (!closedCleanly) {
			return "The log has no closing tag, so it is truncated and some evidence is missing. " + remedy();
		}

		if (!parseTrees) {
			return "This log contains no <parse> trees, so no inlining, intrinsic or escape-analysis "
					+ "evidence is available. " + remedy();
		}

		return null;
	}

	public Map<String, Object> toEvidence() {
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("inlining", parseTrees);
		out.put("compile_times", taskBodies);
		out.put("deoptimisation", true);
		out.put("c2_present", c2);
		out.put("log_closed_cleanly", closedCleanly);
		out.put("unresolved_classes", unresolvedClasses);
		out.put("model_complete", !hasSignificantUnresolved());

		if (tieredStopAtLevel != null) {
			out.put("tiered_stop_at_level", tieredStopAtLevel);
		}

		return out;
	}

}
