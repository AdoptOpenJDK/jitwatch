package com.chrisnewland.jitwatch.mcp.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One pass over the raw log, recovering what the parser and the model do not keep.
 *
 * Two things make this necessary. The HotSpot parser dispatches ten tags and drops the
 * rest, so a top level uncommon_trap never reaches the model even though it carries the
 * reason and action, which on a log without parse trees is the only evidence there is.
 * And a tool cannot tell "the compiler recorded nothing" from "this log cannot contain
 * it" without knowing which tags are present at all.
 */
public final class LogScan {

	private static final int MAX_TRAPS = 20_000;

	private final Map<String, Integer> tagCounts = new LinkedHashMap<>();

	private final List<Trap> traps = new ArrayList<>();

	private boolean closedCleanly;

	private LogScan() {
	}

	public record Trap(String reason, String action, String compileId, String compiler, String level) {
	}

	public static LogScan of(Path logFile) {
		LogScan scan = new LogScan();

		try (BufferedReader reader = Files.newBufferedReader(logFile, StandardCharsets.UTF_8)) {
			String line;

			while ((line = reader.readLine()) != null) {
				scan.consume(line);
			}
		}
		catch (IOException e) {
			// A log that cannot be re-read still parsed once, so the session stands and
			// the capability set simply reports what little this scan established.
		}

		return scan;
	}

	private void consume(String line) {
		int open = line.indexOf('<');

		if (open < 0) {
			return;
		}

		if (line.contains("</hotspot_log>") || line.contains("<hotspot_log_done")) {
			closedCleanly = true;
		}

		String tag = tagName(line, open);

		if (tag == null) {
			return;
		}

		tagCounts.merge(tag, 1, Integer::sum);

		if ("uncommon_trap".equals(tag) && traps.size() < MAX_TRAPS) {
			traps.add(new Trap(attribute(line, "reason"), attribute(line, "action"), attribute(line, "compile_id"),
					attribute(line, "compiler"), attribute(line, "level")));
		}
	}

	private static String tagName(String line, int open) {
		int i = open + 1;

		if (i >= line.length() || !Character.isLetter(line.charAt(i))) {
			return null;
		}

		int end = i;

		while (end < line.length()) {
			char c = line.charAt(end);

			if (c == ' ' || c == '>' || c == '/') {
				break;
			}

			end++;
		}

		return line.substring(i, end);
	}

	private static String attribute(String line, String name) {
		String needle = name + "='";

		int at = line.indexOf(needle);

		if (at < 0) {
			return null;
		}

		int from = at + needle.length();
		int to = line.indexOf('\'', from);

		return to < 0 ? null : line.substring(from, to);
	}

	public int count(String tag) {
		return tagCounts.getOrDefault(tag, 0);
	}

	public List<Trap> traps() {
		return traps;
	}

	public boolean isClosedCleanly() {
		return closedCleanly;
	}

}
