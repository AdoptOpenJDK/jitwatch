package com.chrisnewland.jitwatch.mcp.findings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.chrisnewland.jitwatch.model.Compilation;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.MetaPackage;

import com.chrisnewland.jitwatch.mcp.query.Decision;
import com.chrisnewland.jitwatch.mcp.query.QueryService;
import com.chrisnewland.jitwatch.mcp.session.Session;
import com.chrisnewland.jitwatch.mcp.threshold.InlineReasons;
import com.chrisnewland.jitwatch.mcp.threshold.ThresholdTable;

/**
 * Classifies what the compiler did into a small vocabulary of things worth telling a
 * person.
 *
 * HotSpot has dozens of reason strings and a busy log has thousands of refusals. Nearly
 * all of them are normal. Four rules keep the list short and true:
 *
 * <ul>
 * <li>Only C2's verdict counts, unless a method never reached C2. C1 refuses almost
 * everything over 35 bytes during warm-up, which says nothing about steady-state
 * performance.</li>
 * <li>A finding reports the call count the profile recorded <em>at that call site</em>,
 * never the caller's own invocation count, which would overstate it by orders of
 * magnitude.</li>
 * <li>Call sites that share a caller, a callee and a reason are one finding listing its
 * sites, not one finding each.</li>
 * <li>Advice must be actionable by the person reading it, so a refusal whose callee is
 * JDK code is reported differently and ranked below anything in their own code.</li>
 * </ul>
 */
public final class FindingsEngine {

	public static final String HOT_METHOD_TOO_LARGE = "HOT_METHOD_TOO_LARGE";

	public static final String MEGAMORPHIC_CALL = "MEGAMORPHIC_CALL";

	public static final String SPECULATION_FAILED = "SPECULATION_FAILED";

	public static final String REPEATED_RECOMPILATION = "REPEATED_RECOMPILATION";

	public static final String NEVER_COMPILED = "NEVER_COMPILED";

	public static final String INLINING_TOO_DEEP = "INLINING_TOO_DEEP";

	public static final String CALLER_ALREADY_LARGE = "CALLER_ALREADY_LARGE";

	/**
	 * Call counts above which a refusal stops being a curiosity and starts being a
	 * problem.
	 */
	private static final long HEAT_HIGH = 1_000_000L;

	private static final long HEAT_MEDIUM = 100_000L;

	private static final String[] PLATFORM_PACKAGES = { "java.", "javax.", "jdk.", "sun.", "com.sun." };

	private FindingsEngine() {
	}

	/**
	 * Walk the log and return findings, worst first.
	 * @param packagePrefix when set, only members whose class starts with this
	 * @param limit how many findings to return
	 */
	public static List<Finding> checkup(Session session, String packagePrefix, int limit) {
		List<IMetaMember> members = compiledMembers(session, packagePrefix);

		List<Finding> findings = new ArrayList<>();

		AtomicInteger ids = new AtomicInteger();

		for (IMetaMember member : members) {
			long heat = QueryService.heatOf(member);

			findings.addAll(inliningFindings(session, member, ids));

			Finding deopt = deoptFinding(member, heat, ids);

			if (deopt != null) {
				findings.add(deopt);
			}

			Finding huge = neverCompiledFinding(session, member, heat, ids);

			if (huge != null) {
				findings.add(huge);
			}
		}

		findings.sort(Comparator.comparingLong(Finding::rankScore)
			.reversed()
			.thenComparing(f -> f.signature == null ? "" : f.signature));

		return findings.size() > limit ? new ArrayList<>(findings.subList(0, limit)) : findings;
	}

	public static List<IMetaMember> compiledMembers(Session session, String packagePrefix) {
		List<IMetaMember> out = new ArrayList<>();

		for (MetaPackage root : session.getModel().getPackageManager().getRootPackages()) {
			collect(root, packagePrefix, out);
		}

		return out;
	}

	private static void collect(MetaPackage metaPackage, String packagePrefix, List<IMetaMember> out) {
		for (MetaClass metaClass : metaPackage.getPackageClasses()) {
			if (packagePrefix != null && !packagePrefix.isBlank()
					&& !metaClass.getFullyQualifiedName().startsWith(packagePrefix)) {
				continue;
			}

			for (IMetaMember member : metaClass.getMetaMembers()) {
				if (member.isCompiled()) {
					out.add(member);
				}
			}
		}

		for (MetaPackage child : metaPackage.getChildPackages()) {
			collect(child, packagePrefix, out);
		}
	}

	/**
	 * Groups call sites that say the same thing about the same callee for the same
	 * reason.
	 */
	private record SiteKey(String callee, String reasonNormalised) {
	}

	private static final class SiteGroup {

		Decision representative;

		final List<Integer> bcis = new ArrayList<>();

		final List<Integer> lines = new ArrayList<>();

		long bestCount;

	}

	private static List<Finding> inliningFindings(Session session, IMetaMember member, AtomicInteger ids) {
		List<Decision> decisions = QueryService.decisionsIn(session, member, -1);

		boolean reachedC2 = false;

		for (Decision decision : decisions) {
			if ("C2".equals(decision.compiler)) {
				reachedC2 = true;
				break;
			}
		}

		// One verdict per call site: C2's when the method reached C2, otherwise the
		// highest tier
		// that looked at it. Reporting C1's refusals for a method C2 later compiled would
		// describe
		// warm-up, not the code that ends up running.
		Map<Integer, Decision> verdicts = new LinkedHashMap<>();

		for (Decision decision : decisions) {
			if (reachedC2 && !"C2".equals(decision.compiler)) {
				continue;
			}

			if (!Decision.NOT_INLINED.equals(decision.outcome) && !Decision.VIRTUAL.equals(decision.outcome)) {
				continue;
			}

			Decision existing = verdicts.get(decision.bci);

			if (existing == null || decision.tier > existing.tier) {
				verdicts.put(decision.bci, decision);
			}
		}

		Map<SiteKey, SiteGroup> grouped = new LinkedHashMap<>();

		for (Decision decision : verdicts.values()) {
			if (decision.reasonNormalised == null) {
				continue;
			}

			SiteKey key = new SiteKey(decision.callee, decision.reasonNormalised);

			SiteGroup group = grouped.computeIfAbsent(key, k -> new SiteGroup());

			if (group.representative == null || decision.tier > group.representative.tier) {
				group.representative = decision;
			}

			group.bcis.add(decision.bci);

			if (decision.line > 0) {
				group.lines.add(decision.line);
			}

			if (decision.callCount != null) {
				group.bestCount = Math.max(group.bestCount, decision.callCount);
			}
		}

		List<Finding> findings = new ArrayList<>();

		for (SiteGroup group : grouped.values()) {
			Finding finding = toFinding(member, group, ids);

			if (finding != null) {
				findings.add(finding);
			}
		}

		return findings;
	}

	private static Finding toFinding(IMetaMember member, SiteGroup group, AtomicInteger ids) {
		Decision decision = group.representative;

		String category = decision.reasonNormalised;

		// The count the profile recorded at this call site. Absent for C1, which does not
		// record
		// one; in that case the finding says so rather than borrowing the caller's own
		// count.
		long callCount = group.bestCount;

		boolean countKnown = callCount > 0;

		Finding finding = new Finding();
		finding.id = "F" + ids.incrementAndGet();
		finding.signature = QueryService.signatureOf(member);
		finding.bci = decision.bci;
		finding.line = decision.line;
		finding.invocations = callCount;
		finding.compileId = decision.compileId;
		finding.actionableByReader = !isPlatformCode(decision.callee);

		finding.evidence.put("reason", decision.reason);
		finding.evidence.put("reason_normalised", decision.reasonNormalised);
		finding.evidence.put("compiler", decision.compiler);
		finding.evidence.put("tier", decision.tier);

		if (decision.flag != null) {
			finding.evidence.put("flag", decision.flag);
		}

		if (decision.limit != null) {
			finding.evidence.put("limit", decision.limit);
		}

		if (decision.actual != null) {
			finding.evidence.put("actual", decision.actual);
		}

		if (decision.callee != null) {
			finding.evidence.put("callee", decision.callee);
		}

		if (group.bcis.size() > 1) {
			finding.evidence.put("call_sites", group.bcis);

			if (!group.lines.isEmpty()) {
				finding.evidence.put("source_lines", group.lines);
			}
		}

		if (!countKnown) {
			finding.evidence.put("call_count", "not recorded; this compiler does not profile call sites");
		}

		String callee = shortName(decision.callee);
		String caller = shortName(finding.signature);

		String where = group.bcis.size() > 1 ? " at " + group.bcis.size() + " call sites in " + caller
				: " from " + caller + (finding.line > 0 ? " line " + finding.line : "");

		switch (category) {
			case InlineReasons.CALLEE_TOO_LARGE: {
				finding.type = HOT_METHOD_TOO_LARGE;
				finding.severity = severityFor(callCount, finding.actionableByReader);

				StringBuilder plain = new StringBuilder();
				plain.append("The JVM could not merge ")
					.append(callee)
					.append(" into the code that calls it")
					.append(where);

				if (decision.actual != null && decision.limit != null) {
					plain.append(", because it is ")
						.append(decision.actual)
						.append(" bytes of bytecode against a limit of ")
						.append(decision.limit);
				}

				plain.append(". ");

				if (countKnown) {
					plain.append("The profile recorded ")
						.append(String.format(Locale.ROOT, "%,d", callCount))
						.append(" calls at this site, each paying the cost of a real method call.");
				}
				else {
					plain.append("Every call pays the cost of a real method call.");
				}

				finding.plain = plain.toString();

				if (!finding.actionableByReader) {
					finding.confidence = "informational: the callee is JDK code you cannot change";
					finding.fix = "Call " + callee + " less often on the hot path, or use a smaller alternative. "
							+ "The method itself is part of the JDK.";
				}
				else if (decision.actual != null && decision.limit != null) {
					finding.fix = "Split " + callee + " so the part on the hot path is under " + decision.limit
							+ " bytes of bytecode, or move the cold work out of it.";
				}
				else {
					finding.fix = "Make " + callee + " smaller, or move its hot path into a smaller method.";
				}

				return finding;
			}

			case InlineReasons.VIRTUAL_CALL:
			case InlineReasons.NO_STATIC_BINDING: {
				finding.type = MEGAMORPHIC_CALL;
				finding.severity = severityFor(callCount, finding.actionableByReader);
				finding.plain = "The call to " + callee + where
						+ " reaches more than one implementation, so the JVM cannot predict which method will run and has to "
						+ "look it up on every call.";
				finding.fix = "On the hot path, arrange for one implementation to reach this call site, or split the loop by type.";
				return finding;
			}

			case InlineReasons.TOO_DEEP:
			case InlineReasons.RECURSIVE_TOO_DEEP: {
				finding.type = INLINING_TOO_DEEP;
				finding.severity = callCount >= HEAT_HIGH ? Finding.SEVERITY_MEDIUM : Finding.SEVERITY_INFO;
				finding.plain = "The chain of calls above " + callee + where
						+ " had already reached the JVM's inlining depth limit"
						+ (decision.limit != null ? " of " + decision.limit : "")
						+ ", so this call was left as a real call.";
				finding.fix = "Shorten the call chain on the hot path.";
				return finding;
			}

			case InlineReasons.CALLER_TOO_LARGE: {
				finding.type = CALLER_ALREADY_LARGE;
				finding.severity = callCount >= HEAT_HIGH ? Finding.SEVERITY_MEDIUM : Finding.SEVERITY_INFO;
				finding.plain = callee
						+ " already has a large body of compiled machine code, so the JVM did not merge it into "
						+ caller + where.replace(" from " + caller, "") + ".";
				finding.fix = "Reduce the size of " + callee + ", or accept the call.";
				return finding;
			}

			default:
				// Real, but not actionable enough to put in front of a person. Still
				// reachable
				// through explain_call_site and get_inlining_tree.
				return null;
		}
	}

	private static Finding deoptFinding(IMetaMember member, long heat, AtomicInteger ids) {
		int decompiles = QueryService.decompileCount(member);

		if (decompiles <= 0) {
			return null;
		}

		Finding finding = new Finding();
		finding.id = "F" + ids.incrementAndGet();
		finding.type = decompiles >= 3 ? REPEATED_RECOMPILATION : SPECULATION_FAILED;

		// Some recompilation is normal while a program warms up, so one or two do not
		// lead the
		// list. A method the JVM keeps changing its mind about is a different matter.
		if (decompiles >= 5) {
			finding.severity = Finding.SEVERITY_HIGH;
		}
		else if (decompiles >= 2) {
			finding.severity = Finding.SEVERITY_MEDIUM;
		}
		else {
			finding.severity = Finding.SEVERITY_INFO;
		}

		finding.signature = QueryService.signatureOf(member);
		finding.invocations = heat;
		finding.invocationsAreCallSite = false;
		finding.actionableByReader = !isPlatformCode(finding.signature);

		Compilation last = member.getLastCompilation();

		if (last != null) {
			finding.compileId = last.getCompileID();
		}

		finding.plain = "The JVM optimised " + shortName(finding.signature)
				+ " for the behaviour it saw during warm-up, then the behaviour changed and the optimised code was thrown away "
				+ decompiles + (decompiles == 1 ? " time" : " times") + ", forcing a recompile."
				+ (decompiles < 3 ? " A small amount of this is normal while a program warms up." : "");

		finding.fix = "Warm up with data that looks like production, and avoid switching modes or types on a hot path once "
				+ "it is running.";

		finding.evidence.put("decompiles", decompiles);
		finding.evidence.put("compilations", member.getCompilations().size());

		return finding;
	}

	private static Finding neverCompiledFinding(Session session, IMetaMember member, long heat, AtomicInteger ids) {
		Integer size = QueryService.bytecodeSize(member);

		long hugeLimit = session.getThresholds().value(ThresholdTable.HUGE_METHOD_LIMIT, 8000);

		if (size == null || size < hugeLimit) {
			return null;
		}

		Finding finding = new Finding();
		finding.id = "F" + ids.incrementAndGet();
		finding.type = NEVER_COMPILED;
		finding.severity = Finding.SEVERITY_CRITICAL;
		finding.confidence = "would explain a slowdown";
		finding.signature = QueryService.signatureOf(member);
		finding.invocations = heat;
		finding.invocationsAreCallSite = false;
		finding.actionableByReader = !isPlatformCode(finding.signature);
		finding.plain = "This method is " + size + " bytes of bytecode, over the JVM's " + hugeLimit
				+ "-byte limit for compiling a method at all. It runs interpreted, which is far slower than compiled code.";
		finding.fix = "Split the method into smaller ones.";
		finding.evidence.put("bytecode_size", size);
		finding.evidence.put("limit", hugeLimit);
		finding.evidence.put("flag", ThresholdTable.HUGE_METHOD_LIMIT);

		return finding;
	}

	/** Platform code: the reader of a finding cannot edit it, so advice must differ. */
	public static boolean isPlatformCode(String signature) {
		if (signature == null) {
			return false;
		}

		for (String prefix : PLATFORM_PACKAGES) {
			if (signature.startsWith(prefix)) {
				return true;
			}
		}

		return false;
	}

	private static String severityFor(long callCount, boolean actionable) {
		if (!actionable) {
			// True, but the reader cannot act on it, so it never leads the list
			return Finding.SEVERITY_INFO;
		}

		if (callCount >= HEAT_HIGH) {
			return Finding.SEVERITY_HIGH;
		}

		if (callCount >= HEAT_MEDIUM) {
			return Finding.SEVERITY_MEDIUM;
		}

		return Finding.SEVERITY_INFO;
	}

	/**
	 * Trims a fully qualified name down to Class.member, which is what a person
	 * recognises.
	 */
	public static String shortName(String signature) {
		if (signature == null) {
			return "this method";
		}

		String name = signature;

		int paren = name.indexOf('(');

		String head = paren >= 0 ? name.substring(0, paren) : name;

		int lastDot = head.lastIndexOf('.');

		if (lastDot > 0) {
			int prevDot = head.lastIndexOf('.', lastDot - 1);

			if (prevDot >= 0) {
				head = head.substring(prevDot + 1);
			}
		}

		return head;
	}

	public static boolean nothingActionable(List<Finding> findings) {
		for (Finding finding : findings) {
			if (finding.severityRank() > 1) {
				return false;
			}
		}

		return true;
	}

}
