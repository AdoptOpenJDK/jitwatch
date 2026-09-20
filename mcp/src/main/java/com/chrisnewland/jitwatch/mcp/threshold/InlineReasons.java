package com.chrisnewland.jitwatch.mcp.threshold;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps the wording HotSpot uses for an inlining decision onto a stable category, the flag
 * that decided it, and one sentence of plain language.
 *
 * The point is that C1 and C2 describe the same event differently. C1 says "callee is too
 * large" against {@code MaxInlineSize}; C2 says "hot method too big" against
 * {@code FreqInlineSize}. A model that sees only the raw string will treat those as two
 * findings, or guess the number. The raw string is always carried alongside the category
 * so nothing is lost.
 *
 * The reason strings are those emitted by HotSpot and recognised by JITWatch's own
 * suggestion report; anything not listed here passes through as {@link #OTHER} with its
 * text intact.
 */
public final class InlineReasons {

	/**
	 * The call was inlined. Kept in the same vocabulary so every decision has a category.
	 */
	public static final String INLINED = "INLINED";

	public static final String CALLEE_TOO_LARGE = "CALLEE_TOO_LARGE";

	public static final String CALLER_TOO_LARGE = "CALLER_TOO_LARGE";

	public static final String TOO_DEEP = "TOO_DEEP";

	public static final String RECURSIVE_TOO_DEEP = "RECURSIVE_TOO_DEEP";

	public static final String VIRTUAL_CALL = "VIRTUAL_CALL";

	public static final String NO_STATIC_BINDING = "NO_STATIC_BINDING";

	public static final String NOT_INLINEABLE = "NOT_INLINEABLE";

	public static final String NATIVE = "NATIVE";

	public static final String LOW_FREQUENCY = "LOW_FREQUENCY";

	public static final String NEVER_EXECUTED = "NEVER_EXECUTED";

	public static final String STACK_TOO_LARGE = "STACK_TOO_LARGE";

	public static final String NODE_COUNT = "NODE_COUNT";

	public static final String POLICY = "POLICY";

	public static final String THROWABLE_CTOR = "THROWABLE_CTOR";

	public static final String UNLOADED = "UNLOADED";

	public static final String OTHER = "OTHER";

	/**
	 * @param category one of the constants above
	 * @param flag the HotSpot flag that decided it, or null when no threshold applies
	 * @param plain one sentence, written for someone who has not read the HotSpot sources
	 * @param actionable whether this is worth showing to a user as a finding
	 */
	public record Reason(String category, String flag, String plain, boolean actionable) {
	}

	private static final Map<String, Reason> BY_TEXT = new HashMap<>();

	private static void add(String text, String category, String flag, String plain, boolean actionable) {
		BY_TEXT.put(text.toLowerCase(Locale.ROOT), new Reason(category, flag, plain, actionable));
	}

	static {
		// Size of the callee
		add("hot method too big", CALLEE_TOO_LARGE, ThresholdTable.FREQ_INLINE_SIZE,
				"The call site is hot, but the callee is larger than the limit for inlining a hot method.", true);

		add("too big", CALLEE_TOO_LARGE, ThresholdTable.MAX_INLINE_SIZE,
				"The callee is larger than the limit for inlining at a call site that is not known to be hot.", true);

		add("callee is too large", CALLEE_TOO_LARGE, ThresholdTable.MAX_INLINE_SIZE,
				"The callee is larger than the C1 compiler will inline.", true);

		add("size > desiredmethodlimit", CALLEE_TOO_LARGE, null,
				"The callee exceeds the compiler's desired method size.", true);

		// Size of the caller, or of code already compiled for the callee
		add("already compiled into a big method", CALLER_TOO_LARGE, ThresholdTable.INLINE_SMALL_CODE,
				"The callee already has a large body of compiled native code, so inlining it would bloat the caller.",
				true);

		add("already compiled into a medium method", CALLER_TOO_LARGE, ThresholdTable.INLINE_SMALL_CODE,
				"The callee already has a sizeable body of compiled native code, so it was not inlined here.", true);

		// Depth
		add("inlining too deep", TOO_DEEP, ThresholdTable.MAX_INLINE_LEVEL,
				"The chain of inlined calls reached the compiler's depth limit before this call.", true);

		add("recursive inlining too deep", RECURSIVE_TOO_DEEP, ThresholdTable.MAX_RECURSIVE_INLINE_LEVEL,
				"The method calls itself, and the compiler will only inline a recursive call so far.", true);

		add("recursive inlining is too deep", RECURSIVE_TOO_DEEP, ThresholdTable.MAX_RECURSIVE_INLINE_LEVEL,
				"The method calls itself, and the compiler will only inline a recursive call so far.", true);

		add("recursively inlining too deep", RECURSIVE_TOO_DEEP, ThresholdTable.MAX_RECURSIVE_INLINE_LEVEL,
				"The method calls itself, and the compiler will only inline a recursive call so far.", true);

		// Type of the receiver
		add("virtual call", VIRTUAL_CALL, null,
				"Several different classes arrive at this call site, so the compiler cannot predict which method will run and has to look it up at run time.",
				true);

		add("no static binding", NO_STATIC_BINDING, null,
				"The compiler could not prove which implementation this call reaches, so it left it as a real call.",
				true);

		// Not eligible at all
		add("not inlineable", NOT_INLINEABLE, null, "The callee cannot be inlined.", false);

		add("native method", NATIVE, null, "The callee is a native method, so there is no bytecode to inline.", false);

		add("unloaded signature classes", UNLOADED, null,
				"A class in the callee's signature was not loaded yet, so the compiler could not inline it.", false);

		add("not compilable (disabled)", NOT_INLINEABLE, null, "Compilation is disabled for this method.", false);

		add("don't inline throwable constructors", THROWABLE_CTOR, null,
				"HotSpot never inlines exception constructors.", false);

		// Not worth it
		add("executed < mininliningthreshold times", LOW_FREQUENCY, ThresholdTable.MIN_INLINING_THRESHOLD,
				"The callee had not been called enough times for the compiler to consider it worth inlining.", false);

		add("low call site frequency", LOW_FREQUENCY, null,
				"This call site is cold relative to the rest of the method.", false);

		add("never executed", NEVER_EXECUTED, null, "The profile shows this call was never taken.", false);

		add("call site not reached", NEVER_EXECUTED, null, "The profile shows execution never reached this call.",
				false);

		add("not an accessor", NOT_INLINEABLE, null, "The callee is not a simple accessor.", false);

		// Compiler resources
		add("callee uses too much stack", STACK_TOO_LARGE, ThresholdTable.C1_INLINE_STACK_LIMIT,
				"The callee needs more stack and locals than C1 will absorb into a caller.", true);

		add("nodecountinliningcutoff", NODE_COUNT, ThresholdTable.NODE_COUNT_INLINING_CUTOFF,
				"The compilation had already grown too large for the compiler to keep inlining.", true);

		add("inlining prohibited by policy", POLICY, null,
				"The compiler's inlining policy refused this call. Common causes are a method marked as not compilable and compiler directives.",
				false);
	}

	private InlineReasons() {
	}

	public static Reason lookup(String rawReason) {
		if (rawReason == null) {
			return new Reason(OTHER, null, "The compiler did not record a reason.", false);
		}

		Reason found = BY_TEXT.get(rawReason.toLowerCase(Locale.ROOT).trim());

		if (found != null) {
			return found;
		}

		return new Reason(OTHER, null, "HotSpot reported: " + rawReason, false);
	}

	/**
	 * The limit that applied to a refusal, or null when the reason has no threshold
	 * behind it.
	 */
	public static Long limitFor(Reason reason, Thresholds thresholds) {
		if (reason.flag() == null || thresholds == null) {
			return null;
		}

		Thresholds.Flag flag = thresholds.get(reason.flag());

		return flag == null ? null : flag.value();
	}

}
