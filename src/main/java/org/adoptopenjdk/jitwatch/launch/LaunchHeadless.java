/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.launch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.HotSpotLogParser;
import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.core.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCall;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCallVisitable;
import org.adoptopenjdk.jitwatch.suggestion.AttributeSuggestionWalker;
import org.adoptopenjdk.jitwatch.suggestion.Suggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaunchHeadless implements IJITListener, ILogParseErrorListener
{
	private boolean showErrors;
	private boolean showSuggestions;
	private boolean showOptimizedVirtualCalls;

	private HotSpotLogParser parser;
	private JITWatchConfig config;

	private static final Logger logger = LoggerFactory.getLogger(LaunchHeadless.class);

	public LaunchHeadless(String[] args) throws IOException
	{
		String logFile = args[args.length - 1];

		parseOptions(args);

		config = new JITWatchConfig();

		parser = new HotSpotLogParser(this);
		parser.setConfig(config);

		parser.processLogFile(new File(logFile), this);
	}

	@Override
	public void handleLogEntry(String entry)
	{
		logger.info(entry);
	}

	@Override
	public void handleErrorEntry(String entry)
	{
		if (showErrors)
		{
			logger.error(entry);
		}
	}

	@Override
	public void handleJITEvent(JITEvent event)
	{
		logger.info(event.toString());
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length < 1)
		{
			System.err.println("Usage: LaunchHeadless <options> <hotspot log file>");
			System.err.println("options:");
			System.err.println("-e\tShow parse errors");
			System.err.println("-s\tShow code suggestions");
			System.err.println("-o\tShow optimized virtual calls");

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
			case "-e":
				showErrors = true;
				break;
			case "-s":
				showSuggestions = true;
				break;
			case "-o":
				showOptimizedVirtualCalls = true;
				break;
			}
		}
	}

	@Override
	public void handleReadComplete()
	{
		logger.info("Finished reading log file.");

		if (showSuggestions)
		{
			AttributeSuggestionWalker walker = new AttributeSuggestionWalker(parser.getModel());

			List<Suggestion> suggestions = walker.getSuggestionList();

			showSuggestions(suggestions);
		}

		if (showOptimizedVirtualCalls)
		{
			OptimizedVirtualCallVisitable optimizedVCallVisitable = new OptimizedVirtualCallVisitable();

			List<OptimizedVirtualCall> optimizedVirtualCalls = optimizedVCallVisitable.buildOptimizedCalleeReport(parser.getModel(), config.getAllClassLocations());

			showOptimizedVCalls(optimizedVirtualCalls);
		}
	}

	private void showSuggestions(List<Suggestion> suggestions)
	{
		for (Suggestion suggestion : suggestions)
		{
			String caller;

			if (suggestion.getCaller() != null)
			{
				caller = suggestion.getCaller().getFullyQualifiedMemberName();
			}
			else
			{
				caller = "Unknown";
			}

			StringBuilder builder = new StringBuilder();

			builder.append("\n===================================================");
			builder.append("\nSuggestion: ").append(suggestion.getType());
			builder.append("\n===================================================");
			builder.append("\nScore    : ").append(suggestion.getScore());
			builder.append("\nCaller   : ").append(caller);
			builder.append("\nBytecode : ").append(suggestion.getBytecodeOffset());
			builder.append("\n---------------------------------------------------");
			builder.append("\n").append(suggestion.getText());
			builder.append("\n---------------------------------------------------");

			logger.info(builder.toString());

		}
	}

	private void showOptimizedVCalls(List<OptimizedVirtualCall> vCalls)
	{
		for (OptimizedVirtualCall vCall : vCalls)
		{
			StringBuilder builder = new StringBuilder();

			builder.append("\n===================================================");
			builder.append("\nOptimized Virtual Call");
			builder.append("\n===================================================");
			builder.append("\nCaller      : ").append(vCall.getCallerMember().getMetaClass().getFullyQualifiedName()).append(" : ").append(vCall.getCallerMember().toString());
			builder.append("\nSource Line : ").append(vCall.getCallsite().getSourceLine());
			builder.append("\nBytecode    : ").append(vCall.getCallsite().getBytecodeOffset());
			builder.append("\nInstruction : ").append(vCall.getBytecodeInstruction().getOpcode());
			builder.append("\nCallee      : ").append(vCall.getCallerMember().getMetaClass().getFullyQualifiedName()).append(" : ").append(vCall.getCalleeMember().toString());
			builder.append("\n---------------------------------------------------");

			logger.info(builder.toString());
		}
	}

	@Override
	public void handleReadStart()
	{

	}

	@Override
	public void handleError(String title, String body)
	{
		logger.info("Parse Error: {}.{}", title, body);
	}
}