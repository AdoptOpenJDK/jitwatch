package com.chrisnewland.jitwatch.mcp.threshold;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * The HotSpot compilation flags in effect for one log, with the origin of each value.
 *
 * A flag is either a documented default for the JDK major version that wrote the log, or
 * an override parsed from the {@code <args>} element the JVM recorded at the top of the
 * log. Every inlining refusal the server reports carries the limit that actually applied,
 * so a model never has to recall "is FreqInlineSize 325 on this release".
 */
public final class Thresholds {

	/**
	 * One flag: its value, where the value came from, and one line on what it controls.
	 */
	public record Flag(String name, long value, String source, String meaning) {
		public static final String DEFAULT = "jdk-default";

		public static final String OVERRIDE = "vm-command-override";

	}

	private final int jdkMajor;

	private final boolean jdkMajorExact;

	private final Map<String, Flag> flags;

	Thresholds(int jdkMajor, boolean jdkMajorExact, Map<String, Flag> flags) {
		this.jdkMajor = jdkMajor;
		this.jdkMajorExact = jdkMajorExact;
		this.flags = new TreeMap<>(flags);
	}

	public int getJdkMajor() {
		return jdkMajor;
	}

	/**
	 * False when the log's JDK major was unknown or unlisted and the nearest lower entry
	 * was used.
	 */
	public boolean isJdkMajorExact() {
		return jdkMajorExact;
	}

	public Flag get(String name) {
		return flags.get(name);
	}

	public long value(String name, long fallback) {
		Flag flag = flags.get(name);
		return flag == null ? fallback : flag.value();
	}

	public Map<String, Flag> all() {
		return flags;
	}

	public Map<String, Object> toEvidence() {
		Map<String, Object> out = new LinkedHashMap<>();

		for (Flag flag : flags.values()) {
			Map<String, Object> one = new LinkedHashMap<>();
			one.put("value", flag.value());
			one.put("source", flag.source());
			one.put("meaning", flag.meaning());
			out.put(flag.name(), one);
		}

		return out;
	}

}
