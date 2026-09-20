package com.chrisnewland.jitwatch.mcp.session;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chrisnewland.jitwatch.core.IJITListener;
import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.JITEvent;
import com.chrisnewland.jitwatch.parser.ILogParseErrorListener;
import com.chrisnewland.jitwatch.parser.ILogParser;
import com.chrisnewland.jitwatch.parser.ParserFactory;
import com.chrisnewland.jitwatch.parser.ParserType;

import com.chrisnewland.jitwatch.mcp.threshold.ThresholdTable;
import com.chrisnewland.jitwatch.mcp.threshold.Thresholds;

/**
 * Parses logs into sessions and keeps a bounded number of them in memory.
 *
 * A parsed model for a real service log can be hundreds of megabytes, so the store evicts
 * the least recently used session once the bound is reached rather than growing without
 * limit.
 */
public final class SessionStore {

	/**
	 * How many lines at the head of a log to scan for the JVM's own version and
	 * arguments.
	 */
	private static final int HEAD_LINES = 400;

	private static final Pattern RELEASE = Pattern.compile("<release>\\s*([^<\\s][^<]*?)\\s*</release>",
			Pattern.DOTALL);

	private static final Pattern ARGS = Pattern.compile("<args>\\s*(.*?)\\s*</args>", Pattern.DOTALL);

	private static final Pattern JAVA_VM_VERSION = Pattern.compile("java\\.vm\\.version=([^\\s<]+)");

	private final int maxSessions;

	private final AtomicInteger counter = new AtomicInteger();

	private final Map<String, Session> sessions = new LinkedHashMap<>();

	public SessionStore(int maxSessions) {
		this.maxSessions = Math.max(1, maxSessions);
	}

	public synchronized Collection<Session> all() {
		return new ArrayList<>(sessions.values());
	}

	public synchronized Session get(String id) {
		Session session = sessions.get(id);

		if (session != null) {
			session.touch();
		}

		return session;
	}

	public synchronized boolean close(String id) {
		return sessions.remove(id) != null;
	}

	/**
	 * Parse a log and register the result.
	 * @param logFile the HotSpot log written with -XX:+LogCompilation
	 * @param sourceRoots directories or archives holding the Java source, may be empty
	 * @param classRoots directories or jars holding the compiled classes, may be empty
	 * @param parserType hotspot, j9 or zing
	 */
	public Session load(File logFile, List<String> sourceRoots, List<String> classRoots, ParserType parserType)
			throws IOException {
		JITWatchConfig config = new JITWatchConfig();
		config.setSourceLocations(new ArrayList<>(sourceRoots));
		config.setClassLocations(new ArrayList<>(classRoots));

		List<String> parseErrors = new ArrayList<>();

		// parseErrors is capped for the answer, but the tally must not be: a classpath
		// that resolves nothing produces thousands of these and the caller has to know.
		ParseErrorTally tally = new ParseErrorTally();

		IJITListener jitListener = new IJITListener() {
			@Override
			public void handleLogEntry(String entry) {
			}

			@Override
			public void handleErrorEntry(String entry) {
				tally.add(entry);
				collect(parseErrors, entry);
			}

			@Override
			public void handleJITEvent(JITEvent event) {
			}

			@Override
			public void handleReadStart() {
			}

			@Override
			public void handleReadComplete() {
			}
		};

		ILogParseErrorListener errorListener = (title, body) -> {
			tally.add(title);
			collect(parseErrors, title + ": " + body);
		};

		ILogParser parser = ParserFactory.getParser(parserType, jitListener);
		parser.setConfig(config);
		parser.processLogFile(logFile, errorListener);

		Head head = readHead(logFile.toPath());

		int jdkMajor = parser.getModel().getJDKMajorVersion();

		if (jdkMajor <= 0) {
			jdkMajor = majorFromRelease(head.release());
		}

		Thresholds thresholds = ThresholdTable.resolve(jdkMajor, head.args());

		SignatureIndex index = new SignatureIndex(parser.getModel());

		// One pass over the raw file for what the parser drops: top level uncommon_trap
		// tags, and the tag census that decides what this log can be asked.
		LogScan scan = LogScan.of(logFile.toPath());

		Capabilities capabilities = Capabilities.of(scan, head.args(), tally);

		String id = "s" + counter.incrementAndGet();

		Session session = new Session(id, logFile.getAbsolutePath(), parser.getModel(), config, thresholds, index,
				head.release(), head.args(), parseErrors, scan, capabilities);

		// The parser holds the raw split log for the UI's benefit; the server never
		// serves it
		parser.discardParsedLogs();

		register(session);

		return session;
	}

	/**
	 * Counts parse errors by kind, without the cap that applies to the reported sample.
	 */
	public static final class ParseErrorTally {

		private int total;

		private int unresolvedClasses;

		void add(String entry) {
			if (entry == null) {
				return;
			}

			total++;

			if (entry.contains("MetaMember not found") || entry.contains("ClassNotFoundException")
					|| entry.contains("NoClassDefFoundError")) {
				unresolvedClasses++;
			}
		}

		public int total() {
			return total;
		}

		public int unresolvedClasses() {
			return unresolvedClasses;
		}

	}

	private static void collect(List<String> errors, String entry) {
		if (entry != null && errors.size() < 20) {
			errors.add(entry);
		}
	}

	private synchronized void register(Session session) {
		sessions.put(session.getId(), session);

		while (sessions.size() > maxSessions) {
			sessions.values()
				.stream()
				.min(Comparator.comparingLong(Session::getLastUsed))
				.map(Session::getId)
				.ifPresent(sessions::remove);
		}
	}

	private record Head(String release, String args) {
	}

	/** The version and command line the JVM recorded at the top of the log. */
	private static Head readHead(Path path) throws IOException {
		StringBuilder builder = new StringBuilder();

		try (LineNumberReader reader = new LineNumberReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
			String line;

			while ((line = reader.readLine()) != null && reader.getLineNumber() <= HEAD_LINES) {
				builder.append(line).append('\n');
			}
		}
		catch (java.nio.charset.MalformedInputException mie) {
			return new Head(null, null);
		}

		String head = builder.toString();

		String release = firstGroup(RELEASE, head);

		if (release == null) {
			release = firstGroup(JAVA_VM_VERSION, head);
		}

		return new Head(release, firstGroup(ARGS, head));
	}

	private static String firstGroup(Pattern pattern, String text) {
		Matcher matcher = pattern.matcher(text);

		return matcher.find() ? matcher.group(1).trim() : null;
	}

	/** "21.0.2+13-58" or "1.8.0_402" to a major version. */
	static int majorFromRelease(String release) {
		if (release == null || release.isEmpty()) {
			return -1;
		}

		Matcher matcher = Pattern.compile("^(\\d+)(?:\\.(\\d+))?").matcher(release.trim());

		if (!matcher.find()) {
			return -1;
		}

		int first = Integer.parseInt(matcher.group(1));

		if (first == 1 && matcher.group(2) != null) {
			return Integer.parseInt(matcher.group(2));
		}

		return first;
	}

	/** Rejects anything outside the allowed roots. */
	public static File resolveReadable(String rawPath, List<Path> allowedRoots) throws IOException {
		if (rawPath == null || rawPath.isBlank()) {
			throw new IOException("No path given");
		}

		Path path = Paths.get(rawPath).toAbsolutePath().normalize();

		if (!Files.exists(path)) {
			throw new IOException("Path does not exist: " + path);
		}

		Path real = path.toRealPath();

		if (!allowedRoots.isEmpty()) {
			boolean inside = false;

			for (Path root : allowedRoots) {
				if (real.startsWith(root)) {
					inside = true;
					break;
				}
			}

			if (!inside) {
				throw new IOException("Path is outside the allowed roots: " + real);
			}
		}

		return real.toFile();
	}

}
