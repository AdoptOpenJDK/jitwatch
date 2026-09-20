package com.chrisnewland.jitwatch.mcp.query;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What the compiler did at one call site, in one compilation.
 *
 * The raw reason string is always kept beside the normalised category, because an expert
 * reads "already compiled into a big method" and knows what it means, while a model needs
 * the category and the number.
 */
public final class Decision {

	public static final String INLINED = "INLINED";

	public static final String NOT_INLINED = "NOT_INLINED";

	public static final String INTRINSIC = "INTRINSIC";

	public static final String VIRTUAL = "VIRTUAL";

	public String outcome;

	public int bci;

	public int line = -1;

	public String callee;

	public Integer calleeBytecodeSize;

	public String reason;

	public String reasonNormalised;

	public String flag;

	public Long limit;

	public Long actual;

	public Long callCount;

	public String compileId;

	public String compiler;

	public int tier;

	public boolean osr;

	public String plain;

	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("outcome", outcome);
		map.put("bci", bci);

		if (line > 0) {
			map.put("line", line);
		}

		put(map, "callee", callee);
		put(map, "callee_bytecode_size", calleeBytecodeSize);
		put(map, "reason", reason);
		put(map, "reason_normalised", reasonNormalised);
		put(map, "flag", flag);
		put(map, "limit", limit);
		put(map, "actual", actual);
		put(map, "call_count", callCount);

		map.put("in_compilation", compileId);
		map.put("compiler", compiler);
		map.put("tier", tier);

		if (osr) {
			map.put("osr", true);
		}

		put(map, "plain", plain);

		return map;
	}

	private static void put(Map<String, Object> map, String key, Object value) {
		if (value != null) {
			map.put(key, value);
		}
	}

}
