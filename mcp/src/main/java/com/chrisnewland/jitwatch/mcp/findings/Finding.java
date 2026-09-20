package com.chrisnewland.jitwatch.mcp.findings;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One thing worth telling a person about, in their words, with the evidence attached.
 *
 * A finding always carries somewhere to look ({@code file} is not always resolvable, but
 * the class, member and either a line or a bytecode index always are) and a drill-down
 * handle so the same finding can be taken to the evidence-level tools without searching
 * again.
 */
public final class Finding {

	public static final String SEVERITY_CRITICAL = "critical";

	public static final String SEVERITY_HIGH = "high";

	public static final String SEVERITY_MEDIUM = "medium";

	public static final String SEVERITY_INFO = "info";

	public String id;

	public String type;

	public String severity = SEVERITY_INFO;

	public String confidence = "could explain a slowdown";

	public String plain;

	public String fix;

	public String signature;

	public int bci = -1;

	public int line = -1;

	public long invocations;

	/**
	 * True when {@link #invocations} counts calls at one call site, false when it counts
	 * invocations of the whole method. Reporting a method's own invocation count as if it
	 * were a call-site count overstates the finding by orders of magnitude, so the two
	 * are never mixed.
	 */
	public boolean invocationsAreCallSite = true;

	/** False when the finding is about code the reader cannot edit, such as the JDK. */
	public boolean actionableByReader = true;

	public String compileId;

	public Map<String, Object> evidence = new LinkedHashMap<>();

	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("finding_id", id);
		map.put("type", type);
		map.put("severity", severity);
		map.put("confidence", confidence);
		map.put("plain", plain);

		if (fix != null) {
			map.put("fix", fix);
		}

		Map<String, Object> where = new LinkedHashMap<>();
		where.put("signature", signature);

		if (line > 0) {
			where.put("line", line);
		}

		if (bci >= 0) {
			where.put("bci", bci);
		}

		map.put("where", where);

		if (invocations > 0) {
			map.put(invocationsAreCallSite ? "calls_at_this_site" : "method_invocations", invocations);
		}

		if (!actionableByReader) {
			map.put("in_platform_code", true);
		}

		Map<String, Object> drill = new LinkedHashMap<>();
		drill.put("signature", signature);

		if (bci >= 0) {
			drill.put("bci", bci);
		}

		if (compileId != null) {
			drill.put("compile_id", compileId);
		}

		drill.put("next", "explain_call_site or get_inlining_tree with these arguments");
		map.put("drill", drill);

		if (!evidence.isEmpty()) {
			map.put("evidence", evidence);
		}

		return map;
	}

	/**
	 * Ordering key for the findings list, highest first.
	 *
	 * Severity leads. Then whether the reader can act on it, because a true statement
	 * about JDK code they cannot edit should never head the list. Then specificity: a
	 * finding that points at one call site on one line is more useful than one about a
	 * whole method, and it also avoids comparing a call-site count against a method
	 * invocation count, which are different units and differ by orders of magnitude. The
	 * count itself only breaks ties within the same unit.
	 */
	public long rankScore() {
		long score = severityRank() * 1_000_000_000_000L;

		if (actionableByReader) {
			score += 100_000_000_000L;
		}

		if (invocationsAreCallSite) {
			score += 10_000_000_000L;
		}

		return score + Math.min(invocations, 9_000_000_000L);
	}

	/** Higher is worse. */
	public int severityRank() {
		switch (severity) {
			case SEVERITY_CRITICAL:
				return 4;
			case SEVERITY_HIGH:
				return 3;
			case SEVERITY_MEDIUM:
				return 2;
			default:
				return 1;
		}
	}

}
