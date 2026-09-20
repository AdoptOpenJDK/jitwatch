package com.chrisnewland.jitwatch.mcp.threshold;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chrisnewland.jitwatch.mcp.threshold.Thresholds.Flag;

/**
 * Resolves the compilation flags in effect for a log: documented defaults for the JDK
 * major version, then any {@code -XX:} override found on the command line the JVM
 * recorded.
 *
 * The defaults below are the HotSpot values for x86_64 and aarch64. They are the ones
 * that decide inlining, so they are the ones worth reporting; anything else a caller
 * needs can be read from the log's own arguments. Values marked in the notes as
 * release-dependent changed across JDK versions and are keyed by major below.
 */
public final class ThresholdTable {

	public static final String MAX_INLINE_SIZE = "MaxInlineSize";

	public static final String FREQ_INLINE_SIZE = "FreqInlineSize";

	public static final String INLINE_SMALL_CODE = "InlineSmallCode";

	public static final String MAX_INLINE_LEVEL = "MaxInlineLevel";

	public static final String MAX_RECURSIVE_INLINE_LEVEL = "MaxRecursiveInlineLevel";

	public static final String MAX_TRIVIAL_SIZE = "MaxTrivialSize";

	public static final String MIN_INLINING_THRESHOLD = "MinInliningThreshold";

	public static final String HUGE_METHOD_LIMIT = "HugeMethodLimit";

	public static final String NODE_COUNT_INLINING_CUTOFF = "NodeCountInliningCutoff";

	public static final String C1_INLINE_STACK_LIMIT = "C1InlineStackLimit";

	private static final Pattern XX_NUMERIC = Pattern.compile("-XX:([A-Za-z0-9_]+)=(-?\\d+)");

	private ThresholdTable() {
	}

	/**
	 * @param jdkMajor the major version from the log, or -1 when it could not be
	 * determined
	 * @param vmArguments the contents of the log's {@code <args>} element, may be null
	 */
	public static Thresholds resolve(int jdkMajor, String vmArguments) {
		int effective = jdkMajor;
		boolean exact = true;

		if (effective < 8) {
			// Unknown or pre-8: fall back to the oldest entry and say so in the answer
			effective = 8;
			exact = false;
		}

		Map<String, Flag> flags = defaults(effective);

		applyOverrides(flags, vmArguments);

		return new Thresholds(jdkMajor, exact, flags);
	}

	private static Map<String, Flag> defaults(int jdkMajor) {
		Map<String, Flag> flags = new LinkedHashMap<>();

		put(flags, MAX_INLINE_SIZE, 35,
				"Largest callee, in bytes of bytecode, that will be inlined at a call site that is not known to be hot.");

		put(flags, FREQ_INLINE_SIZE, 325,
				"Largest callee, in bytes of bytecode, that will be inlined at a hot call site.");

		// InlineSmallCode moved from 2000 to 2500 on 64-bit platforms after JDK 8
		put(flags, INLINE_SMALL_CODE, jdkMajor >= 9 ? 2500 : 2000,
				"If the callee already has compiled native code larger than this, it is not inlined.");

		// JDK-8234863 raised MaxInlineLevel from 9 to 15 in JDK 14
		put(flags, MAX_INLINE_LEVEL, jdkMajor >= 14 ? 15 : 9, "Maximum depth of nested inlining.");

		put(flags, MAX_RECURSIVE_INLINE_LEVEL, 1, "Maximum depth for inlining a method into itself.");

		put(flags, MAX_TRIVIAL_SIZE, 6, "A callee this small is inlined without further checks.");

		put(flags, MIN_INLINING_THRESHOLD, 250, "A callee invoked fewer times than this is not inlined.");

		put(flags, HUGE_METHOD_LIMIT, 8000,
				"A method larger than this is never compiled at all while DontCompileHugeMethods is on. It runs interpreted.");

		put(flags, NODE_COUNT_INLINING_CUTOFF, 18000,
				"C2 stops inlining once the compilation reaches this many nodes.");

		put(flags, C1_INLINE_STACK_LIMIT, 5, "C1 refuses a callee whose stack and locals exceed this.");

		return flags;
	}

	private static void put(Map<String, Flag> flags, String name, long value, String meaning) {
		flags.put(name, new Flag(name, value, Flag.DEFAULT, meaning));
	}

	private static void applyOverrides(Map<String, Flag> flags, String vmArguments) {
		if (vmArguments == null || vmArguments.isEmpty()) {
			return;
		}

		Matcher matcher = XX_NUMERIC.matcher(vmArguments);

		while (matcher.find()) {
			String name = matcher.group(1);

			Flag existing = flags.get(name);

			if (existing != null) {
				long overridden = Long.parseLong(matcher.group(2));

				flags.put(name, new Flag(name, overridden, Flag.OVERRIDE, existing.meaning()));
			}
		}
	}

}
