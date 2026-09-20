package com.chrisnewland.jitwatch.mcp.answer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The one door every answer passes through.
 *
 * A single method in a real log can have dozens of compilations, an inlining tree can be
 * thousands of nodes deep and wide, and a top list can be as long as the log. An agent
 * that receives any of those unbounded has lost its working memory and cannot finish the
 * task it was asked to do. So lists are capped, strings are truncated, and the whole
 * serialised answer is capped again as a backstop.
 *
 * Truncation is never silent: whatever is cut leaves behind a count of what was there
 * and, for lists, the parameter the caller can raise.
 */
public final class Budget {

	private final int maxListItems;

	private final int maxStringChars;

	private final int maxTotalChars;

	public Budget(int maxListItems, int maxStringChars, int maxTotalChars) {
		this.maxListItems = maxListItems;
		this.maxStringChars = maxStringChars;
		this.maxTotalChars = maxTotalChars;
	}

	public static Budget defaults() {
		return new Budget(25, 2_000, 32_000);
	}

	public int maxListItems() {
		return maxListItems;
	}

	public int clampLimit(Integer requested) {
		if (requested == null || requested <= 0) {
			return maxListItems;
		}

		return Math.min(requested, maxListItems * 8);
	}

	/**
	 * Caps a list, replacing the tail with a marker that says how much was left out.
	 * @param key what the list is called in the answer, used in the marker text
	 */
	public <T> List<Object> capList(List<T> items, int limit, String key, Map<String, Object> intoEvidence) {
		List<Object> out = new ArrayList<>();

		int effective = Math.min(limit, maxListItems * 8);

		for (T item : items) {
			if (out.size() >= effective) {
				break;
			}

			out.add(item);
		}

		if (items.size() > out.size() && intoEvidence != null) {
			Map<String, Object> truncated = new LinkedHashMap<>();
			truncated.put("shown", out.size());
			truncated.put("total", items.size());
			truncated.put("hint", "raise the limit argument to see more of " + key);
			intoEvidence.put(key + "_truncated", truncated);
		}

		return out;
	}

	public String capString(String text) {
		if (text == null || text.length() <= maxStringChars) {
			return text;
		}

		return text.substring(0, maxStringChars) + "… [" + (text.length() - maxStringChars) + " more characters]";
	}

	/**
	 * Last-resort cap on the serialised answer.
	 *
	 * The per-list caps above should keep answers small; this exists so that a
	 * pathological log cannot produce a megabyte of JSON through some path nobody
	 * anticipated.
	 */
	public String capTotal(String json) {
		if (json == null || json.length() <= maxTotalChars) {
			return json;
		}

		return json.substring(0, maxTotalChars) + "\n… answer truncated at " + maxTotalChars
				+ " characters. Narrow the query, or lower the limit argument.";
	}

}
