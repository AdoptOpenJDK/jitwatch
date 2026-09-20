package com.chrisnewland.jitwatch.mcp.answer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every tool result, in two halves.
 *
 * {@code plain} is one or two sentences for someone who has never read the HotSpot
 * sources. {@code evidence} is the exact data: compile ids, bytecode indices, raw reason
 * strings, the flag and limit that applied. Both halves are always present, and no tool
 * has an audience switch: the agent decides which half to show the person in front of it.
 */
public final class Answer {

	private final Map<String, Object> evidence = new LinkedHashMap<>();

	private String plain = "";

	private final List<String> notes = new ArrayList<>();

	public static Answer of(String plain) {
		Answer answer = new Answer();
		answer.plain = plain;
		return answer;
	}

	public Answer plain(String text) {
		this.plain = text;
		return this;
	}

	public Answer put(String key, Object value) {
		evidence.put(key, value);
		return this;
	}

	/**
	 * Something the caller should know that is not a finding, such as a fallback the
	 * server made.
	 */
	public Answer note(String note) {
		notes.add(note);
		return this;
	}

	public String getPlain() {
		return plain;
	}

	public Map<String, Object> getEvidence() {
		return evidence;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("plain", plain);
		map.put("evidence", evidence);

		if (!notes.isEmpty()) {
			map.put("notes", notes);
		}

		return map;
	}

}
