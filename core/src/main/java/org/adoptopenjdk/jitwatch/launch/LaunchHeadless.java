/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.launch;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.HEADLESS_SEPARATOR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.HotSpotLogParser;
import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.core.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.inline.HeadlessInlineVisitor;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.report.comparator.ScoreComparator;
import org.adoptopenjdk.jitwatch.report.suggestion.SuggestionWalker;
import org.adoptopenjdk.jitwatch.treevisitor.TreeVisitor;
import org.adoptopenjdk.jitwatch.util.HeadlessUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class LaunchHeadless implements IJITListener, ILogParseErrorListener
{
	private boolean showTimeLine;
	private boolean showErrors;
	private boolean showModel;
	private boolean showOnlyCompiledMethods;
	private boolean showSuggestions;
	private boolean outputFile;
	private boolean showInlineFailedCalls;

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
			timelineBuilder.append(event.getEventMember().toStringUnqualifiedMethodName(true, true)).append(S_NEWLINE);
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
			System.err.println("-c\tShow only compiled methods in model (use with -m)");
			System.err.println("-s\tShow code suggestions");
			System.err.println("-t\tShow compilation timeline");
			System.err.println("-f\tWrite output to headless.csv");
			System.err.println("-i\tShow inline failed calls");
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
				
			case "-s":
				showSuggestions = true;
				break;
				
			case "-t":
				showTimeLine = true;
				break;
				
			case "-f":
				outputFile = true;
				break;
				
			case "-i":
				showInlineFailedCalls = true;
				break;
				
				// case "-o":
				// showOptimizedVirtualCalls = true;
				// break;s
			}
		}
	}

	@Override
	public void handleReadComplete()
	{
		StringBuilder outputBuilder = new StringBuilder();

		if (showTimeLine)
		{
			outputBuilder.append(timelineBuilder.toString()).append(S_NEWLINE);
		}

		if (showErrors)
		{
			outputBuilder.append(errorBuilder.toString()).append(S_NEWLINE);
		}

		if (showModel)
		{
			IReadOnlyJITDataModel model = parser.getModel();

			String modelString = HeadlessUtil.modelToString(model, showOnlyCompiledMethods);

			outputBuilder.append(modelString).append(S_NEWLINE);
		}

		if (showSuggestions)
		{
			SuggestionWalker walker = new SuggestionWalker(parser.getModel());

			List<Report> suggestions = walker.getReports(new ScoreComparator());

			outputBuilder.append(getSuggestions(suggestions));
		}

		if (outputFile)
		{
			outputBuilder.insert(0, "sep=" + HEADLESS_SEPARATOR + S_NEWLINE);

			try
			{
				Files.write(Paths.get("headless.csv"), outputBuilder.toString().getBytes());

				System.out.println("Wrote to headless.csv");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println(outputBuilder.toString());
		}

		if (showInlineFailedCalls)
		{
			HeadlessInlineVisitor inlineVisitor = new HeadlessInlineVisitor(parser.getModel());
			TreeVisitor.walkTree(parser.getModel(), inlineVisitor);
			inlineVisitor.printFailedList(System.out);
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

	private String getSuggestions(List<Report> suggestions)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Type").append(HEADLESS_SEPARATOR);
		builder.append("Score").append(HEADLESS_SEPARATOR);
		builder.append("Caller Class").append(HEADLESS_SEPARATOR);
		builder.append("Caller Member").append(HEADLESS_SEPARATOR);
		builder.append("BCI").append(HEADLESS_SEPARATOR);
		builder.append("Suggestion").append(S_NEWLINE);

		for (Report suggestion : suggestions)
		{
			String callerClass;
			String callerMember;

			if (suggestion.getCaller() != null)
			{
				callerClass = suggestion.getCaller().getMetaClass().getFullyQualifiedName();
				callerMember = suggestion.getCaller().toStringUnqualifiedMethodName(true, true);
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

		return builder.toString();
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