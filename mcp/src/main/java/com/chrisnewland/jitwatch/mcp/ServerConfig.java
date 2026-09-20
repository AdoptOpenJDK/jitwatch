package com.chrisnewland.jitwatch.mcp;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.chrisnewland.freelogj.Logger;

/**
 * Everything the operator chooses when starting the server.
 *
 * A bad argument is warned about and ignored rather than fatal: an MCP host surfaces a
 * server that exited far less clearly than one that started and said what it skipped.
 *
 * @param allowedRoots paths the agent may read; empty means no restriction
 * @param logLevel JITWatch's own log level, not this server's per-call lines
 * @param logFile where JITWatch's logging goes; null keeps it on stderr
 * @param httpPort serve HTTP on this port; 0 means stdio
 */
public record ServerConfig(List<Path> allowedRoots, int maxSessions, int maxListItems, int maxAnswerChars,
		Logger.LogLevel logLevel, Path logFile, int httpPort, List<String> warnings) {

	private static final int DEFAULT_MAX_SESSIONS = 4;

	private static final int DEFAULT_MAX_LIST_ITEMS = 25;

	private static final int DEFAULT_MAX_ANSWER_CHARS = 32_000;

	/**
	 * JITWatch's logging defaults to FATAL, which in practice silences it. It is far too
	 * chatty to sit underneath a tool call: INFO prints a line per javap invocation, WARN
	 * one per tag a visitor declines to handle, ERROR one per class it cannot resolve. A
	 * single call can produce hundreds of lines and bury the answer. --log-level brings
	 * it back when debugging the parser.
	 */
	private static final Logger.LogLevel DEFAULT_LOG_LEVEL = Logger.LogLevel.FATAL;

	public static String usage() {
		return """
				jitwatch-mcp options:
				  --allowed-root <path>     restrict every path argument to this tree; repeatable
				  --http <port>             serve HTTP on this port instead of stdio
				  --max-sessions <n>        loaded logs held at once (default 4)
				  --max-list-items <n>      default list length in an answer (default 25)
				  --max-answer-chars <n>    hard cap on one answer (default 32000)
				  --log-level <level>       JITWatch's own logging: TRACE DEBUG INFO WARN ERROR FATAL (default FATAL)
				  --log-file <path>         send JITWatch's logging to a file instead of stderr""";
	}

	public static ServerConfig parse(String[] args) {
		List<Path> roots = new ArrayList<>();
		int maxSessions = DEFAULT_MAX_SESSIONS;
		int maxListItems = DEFAULT_MAX_LIST_ITEMS;
		int maxAnswerChars = DEFAULT_MAX_ANSWER_CHARS;
		int httpPort = 0;
		Logger.LogLevel logLevel = DEFAULT_LOG_LEVEL;
		Path logFile = null;

		// Parsing happens before logging is configured, so a complaint is carried out
		// and logged once there is somewhere to log it.
		List<String> warnings = new ArrayList<>();

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "--allowed-root":
					if (i + 1 < args.length) {
						roots.add(Paths.get(args[++i]).toAbsolutePath().normalize());
					}
					break;

				case "--max-sessions":
					if (i + 1 < args.length) {
						maxSessions = Integer.parseInt(args[++i]);
					}
					break;

				case "--max-list-items":
					if (i + 1 < args.length) {
						maxListItems = Integer.parseInt(args[++i]);
					}
					break;

				case "--max-answer-chars":
					if (i + 1 < args.length) {
						maxAnswerChars = Integer.parseInt(args[++i]);
					}
					break;

				case "--log-level":
					if (i + 1 < args.length) {
						logLevel = level(args[++i], logLevel, warnings);
					}
					break;

				// Serve HTTP instead of stdio, so the process outlives any one client and
				// a client reaches it by URL rather than being its parent.
				case "--http":
					if (i + 1 < args.length) {
						httpPort = Integer.parseInt(args[++i]);
					}
					break;

				case "--log-file":
					if (i + 1 < args.length) {
						logFile = Paths.get(args[++i]).toAbsolutePath().normalize();
					}
					break;

				default:
					break;
			}
		}

		return new ServerConfig(readable(roots, warnings), maxSessions, maxListItems, maxAnswerChars, logLevel, logFile,
				httpPort, warnings);
	}

	private static Logger.LogLevel level(String name, Logger.LogLevel fallback, List<String> warnings) {
		try {
			return Logger.LogLevel.valueOf(name.toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException e) {
			warnings.add("unknown --log-level '" + name + "', using " + fallback
					+ ". Valid: TRACE DEBUG INFO WARN ERROR FATAL");

			return fallback;
		}
	}

	/** Resolves symlinks so the containment check cannot be walked around. */
	private static List<Path> readable(List<Path> roots, List<String> warnings) {
		List<Path> real = new ArrayList<>();

		for (Path root : roots) {
			try {
				real.add(root.toRealPath());
			}
			catch (Exception e) {
				warnings.add("ignoring unreadable allowed root: " + root);
			}
		}

		return real;
	}

	public boolean wantsStackTraces() {
		return logLevel.ordinal() <= Logger.LogLevel.DEBUG.ordinal();
	}

	public boolean isHttp() {
		return httpPort > 0;
	}

	public String describe(String transport) {
		return "version " + JitWatchMcpServer.VERSION + ", transport " + transport + ", jitwatch log level " + logLevel
				+ ", logging to " + (logFile != null ? logFile.toString() : "stderr") + ", allowed roots "
				+ (allowedRoots.isEmpty() ? "unrestricted" : allowedRoots.toString());
	}
}
