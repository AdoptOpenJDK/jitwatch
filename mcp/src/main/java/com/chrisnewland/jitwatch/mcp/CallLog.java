package com.chrisnewland.jitwatch.mcp;

import java.io.PrintStream;

import com.chrisnewland.freelogj.Logger;
import com.chrisnewland.freelogj.Logger.LogLevel;

/**
 * One line when a tool call starts, one when it ends.
 *
 * It carries its own level rather than the one
 * {@link com.chrisnewland.freelogj.LoggerFactory} sets globally, because that level
 * exists to keep JITWatch quiet: JITWatch logs per javap invocation, per unhandled tag
 * and per unresolvable class, which buries a tool call in hundreds of lines. The call
 * trace has to survive that, so it is configured separately.
 *
 * The stream is stderr because stdout carries the protocol under stdio. The trace lands
 * in whatever console started the server, and a host recording the child's stderr keeps
 * it.
 */
public final class CallLog {

	private static final int MAX_DETAIL = 300;

	private final Logger logger;

	private final boolean stackTraces;

	public CallLog(PrintStream out, boolean stackTraces) {
		this.logger = Logger.getLogger(CallLog.class, LogLevel.INFO, out);
		this.stackTraces = stackTraces;
	}

	public void note(String message) {
		logger.info("{}", message);
	}

	public void started(String tool, Object arguments) {
		if (arguments == null) {
			logger.info("-> {}", tool);
		}
		else {
			logger.info("-> {} {}", tool, abbreviate(String.valueOf(arguments)));
		}
	}

	public void succeeded(String tool, long millis, int chars) {
		logger.info("<- {} ok {}ms {} chars", tool, millis, chars);
	}

	public void rejected(String tool, long millis, String message) {
		logger.warn("<- {} bad-argument {}ms : {}", tool, millis, abbreviate(message));
	}

	/** A stack trace follows when the log level asked for one. */
	public void failed(String tool, long millis, String message, Throwable cause) {
		logger.error("<- {} failed {}ms : {}", tool, millis, abbreviate(message));

		if (stackTraces && cause != null) {
			logger.error("{}", stackTrace(cause));
		}
	}

	private static String stackTrace(Throwable cause) {
		StringBuilder builder = new StringBuilder(cause.toString());

		for (StackTraceElement element : cause.getStackTrace()) {
			builder.append(System.lineSeparator()).append("\tat ").append(element);
		}

		return builder.toString();
	}

	private static String abbreviate(String text) {
		if (text == null) {
			return "";
		}

		String oneLine = text.replace('\n', ' ');

		return oneLine.length() <= MAX_DETAIL ? oneLine : oneLine.substring(0, MAX_DETAIL) + "…";
	}

}
