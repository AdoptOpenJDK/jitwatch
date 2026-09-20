package com.chrisnewland.jitwatch.mcp.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The arguments of one tool call, coerced to the type the tool wants.
 *
 * A host may send a number as a string, a lone string where an array is declared, or omit
 * an optional argument, and the schema validator does not stop it. That defensiveness
 * lives here once: an unusable value reads as null, a missing list as empty.
 */
public final class ToolArguments {

	private final Map<String, Object> values;

	public ToolArguments(Map<String, Object> values) {
		this.values = values == null ? Map.of() : values;
	}

	private Object raw(String key) {
		return values.get(key);
	}

	public String string(String key) {
		Object value = raw(key);

		return value == null ? null : String.valueOf(value);
	}

	public Integer integer(String key) {
		Object value = raw(key);

		if (value instanceof Number number) {
			return number.intValue();
		}

		if (value instanceof String text && !text.isBlank()) {
			try {
				return Integer.valueOf(text.trim());
			}
			catch (NumberFormatException nfe) {
				return null;
			}
		}

		return null;
	}

	public Boolean bool(String key) {
		Object value = raw(key);

		if (value instanceof Boolean flag) {
			return flag;
		}

		if (value instanceof String text) {
			return Boolean.valueOf(text);
		}

		return null;
	}

	public List<String> strings(String key) {
		Object value = raw(key);

		List<String> out = new ArrayList<>();

		if (value instanceof List<?> list) {
			for (Object item : list) {
				if (item != null) {
					out.add(String.valueOf(item));
				}
			}
		}
		else if (value instanceof String text && !text.isBlank()) {
			out.add(text);
		}

		return out;
	}

	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public String toString() {
		return values.toString();
	}

}
