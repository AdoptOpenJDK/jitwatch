/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.launch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import org.adoptopenjdk.jitwatch.core.HotSpotLogParser;
import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.core.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.JITEvent;
//import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCall;
//import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCallVisitable;
import org.adoptopenjdk.jitwatch.suggestion.AttributeSuggestionWalker;
import org.adoptopenjdk.jitwatch.suggestion.Suggestion;
import org.adoptopenjdk.jitwatch.util.HeadlessUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class LaunchHeadless implements IJITListener, ILogParseErrorListener
{
	private boolean showTimeLine;
	private boolean showErrors;
	private boolean showModel;
	private boolean showOnlyCompiledMethods;
	// private boolean showOptimizedVirtualCalls;
	private boolean showSuggestions;

	private HotSpotLogParser parser;
	private JITWatchConfig config;

	private StringBuilder timelineBuilder = new StringBuilder();
	private StringBuilder errorBuilder = new StringBuilder();

	public LaunchHeadless(String[] args) throws IOException
	{
		String logFile = args[args.length - 1];

		parseOptions(args);

		timelineBuilder.append("Timestamp").append(HEADLESS_SEPARATOR);
		timelineBuilder.append("Event").append(HEADLESS_SEPARATOR);
		timelineBuilder.append("Class").append(HEADLESS_SEPARATOR);
		timelineBuilder.append("Member").append(S_NEWLINE);

		config = new JITWatchConfig();

		parser = new HotSpotLogParser(this);
		parser.setConfig(config);

		parser.processLogFile(new File(logFile), this);
	}

	@Override
	public void handleLogEntry(String entry)
	{
	}

	@Override
	public void handleErrorEntry(String entry)
	{
		if (showErrors)
		{
			errorBuilder.append(entry).append(S_NEWLINE);
		}
	}

	@Override
	public void handleJITEvent(JITEvent event)
	{
		if (showTimeLine)
		{
			timelineBuilder.append(StringUtil.formatTimestamp(event.getStamp(), true)).append(HEADLESS_SEPARATOR);
			timelineBuilder.append(event.getEventType().getText()).append(HEADLESS_SEPARATOR);
			timelineBuilder.append(event.getEventMember().getMetaClass().getFullyQualifiedName()).append(HEADLESS_SEPARATOR);
			timelineBuilder.append(event.getEventMember().toStringUnqualifiedMethodName(true)).append(S_NEWLINE);
		}
	}

	@Override
	public void handleReadStart()
	{
	}

	@Override
	public void handleError(String title, String body)
	{
		if (showErrors)
		{
			errorBuilder.append(title).append(HEADLESS_SEPARATOR).append(body).append(S_NEWLINE);
		}
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length < 2)
		{
			System.err.println("Usage: LaunchHeadless <options> <hotspot log file>");
			System.err.println("options:");
			System.err.println("-e\tShow parse errors");
			System.err.println("-m\tShow model");
			System.err.println("\t-c\tShow only compiled methods in model");
			System.err.println("-s\tShow code suggestions");
			System.err.println("-t\tShow compilation timeline");
			// System.err.println("-o\tShow optimized virtual calls");

			System.exit(-1);
		}

		new LaunchHeadless(args);
	}

	private void parseOptions(String[] args)
	{
		for (int i = 0; i < args.length - 1; i++)
		{
			String arg = args[i];

			switch (arg)
			{
			case "-c":
				showOnlyCompiledMethods = true;
				break;
			case "-e":
				showErrors = true;
				break;
			case "-m":
				showModel = true;
				break;
			// case "-o":
			// showOptimizedVirtualCalls = true;
			// break;
			case "-s":
				showSuggestions = true;
				break;
			case "-t":
				showTimeLine = true;
				break;

			}
		}
	}

	@Override
	public void handleReadComplete()
	{
		if (showTimeLine)
		{
			System.out.println(timelineBuilder.toString());
		}

		if (showErrors)
		{
			System.out.println(errorBuilder.toString());
		}

		if (showModel)
		{
			IReadOnlyJITDataModel model = parser.getModel();

			String modelString = HeadlessUtil.modelToString(model, showOnlyCompiledMethods);

			System.out.println(modelString);
		}

		if (showSuggestions)
		{
			AttributeSuggestionWalker walker = new AttributeSuggestionWalker(parser.getModel());

			List<Suggestion> suggestions = walker.getSuggestionList();

			showSuggestions(suggestions);
		}

		// if (showOptimizedVirtualCalls)
		// {
		// OptimizedVirtualCallVisitable optimizedVCallVisitable = new
		// OptimizedVirtualCallVisitable();
		//
		// List<OptimizedVirtualCall> optimizedVirtualCalls =
		// optimizedVCallVisitable.buildOptimizedCalleeReport(parser.getModel(),
		// config.getAllClassLocations());
		//
		// showOptimizedVCalls(optimizedVirtualCalls);
		// }
	}

	private void showSuggestions(List<Suggestion> suggestions)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Type").append(HEADLESS_SEPARATOR);
		builder.append("Score").append(HEADLESS_SEPARATOR);
		builder.append("Caller Class").append(HEADLESS_SEPARATOR);
		builder.append("Caller Member").append(HEADLESS_SEPARATOR);
		builder.append("BCI").append(HEADLESS_SEPARATOR);
		builder.append("Suggestion").append(S_NEWLINE);

		for (Suggestion suggestion : suggestions)
		{
			String callerClass;
			String callerMember;

			if (suggestion.getCaller() != null)
			{
				callerClass = suggestion.getCaller().getMetaClass().getFullyQualifiedName();
				callerMember = suggestion.getCaller().toStringUnqualifiedMethodName(true);
			}
			else
			{
				callerClass = "Unknown";
				callerMember = "Unknown";

			}

			String cleanText = suggestion.getText().replace(C_NEWLINE, C_SPACE);

			builder.append(suggestion.getType()).append(HEADLESS_SEPARATOR);
			builder.append(suggestion.getScore()).append(HEADLESS_SEPARATOR);
			builder.append(callerClass).append(HEADLESS_SEPARATOR);
			builder.append(callerMember).append(HEADLESS_SEPARATOR);
			builder.append(suggestion.getBytecodeOffset()).append(HEADLESS_SEPARATOR);
			builder.append(cleanText).append(S_NEWLINE);
		}

		System.out.println(builder.toString());
	}

	// private void showOptimizedVCalls(List<OptimizedVirtualCall> vCalls)
	// {
	// StringBuilder builder = new StringBuilder();
	//
	// builder.append("Caller class").append(HEADLESS_SEPARATOR);
	// builder.append("Caller member").append(HEADLESS_SEPARATOR);
	// builder.append("Source line").append(HEADLESS_SEPARATOR);
	// builder.append("BCI").append(HEADLESS_SEPARATOR);
	// builder.append("Instruction").append(HEADLESS_SEPARATOR);
	// builder.append("Callee class").append(HEADLESS_SEPARATOR);
	// builder.append("Callee member").append(S_NEWLINE);
	//
	// for (OptimizedVirtualCall vCall : vCalls)
	// {
	// builder.append(vCall.getCallerMember().getMetaClass().getFullyQualifiedName()).append(HEADLESS_SEPARATOR);
	// builder.append(vCall.getCallerMember().toString()).append(HEADLESS_SEPARATOR);
	// builder.append(vCall.getCallsite().getSourceLine()).append(HEADLESS_SEPARATOR);
	// builder.append(vCall.getCallsite().getBytecodeOffset()).append(HEADLESS_SEPARATOR);
	// builder.append(vCall.getBytecodeInstruction().getOpcode()).append(HEADLESS_SEPARATOR);
	// builder.append(vCall.getCallerMember().getMetaClass().getFullyQualifiedName()).append(HEADLESS_SEPARATOR);
	// builder.append(vCall.getCalleeMember().toString()).append(S_NEWLINE);
	// }
	//
	// System.out.println(builder.toString());
	// }
}