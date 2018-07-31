/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.parser.zing;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_QUEUED;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.model.CompilerThread;
import org.adoptopenjdk.jitwatch.model.NumberedLine;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.parser.AbstractLogParser;
import org.adoptopenjdk.jitwatch.util.ParseUtil;

public class ZingLogParser extends AbstractLogParser
{
	private static String ZING_LOG_LINE = "(.*)([0-9]+\\.[0-9]+\\:\\s+)(.*)";

	private static String ZING_WAITED = "(.*)\\s+waited\\s+([0-9]+)\\s+ms(.*)";

	private static String ZING_COMPILE_TIME = "(.*)\\s+compile\\s+time\\s+([0-9]+)\\s+/(.*)";

	private static final Pattern PATTERN_LOG_SIGNATURE = Pattern.compile(ZING_LOG_LINE);

	private static final Pattern PATTERN_WAITED_SIGNATURE = Pattern.compile(ZING_WAITED);

	private static final Pattern PATTERN_COMPILE_TIME_SIGNATURE = Pattern.compile(ZING_COMPILE_TIME);

	private Map<Integer, ZingLine> compileIdMap = new HashMap<>();

	public ZingLogParser(IJITListener jitListener)
	{
		super(jitListener);

		currentCompilerThread = new CompilerThread("dummy", "dummy");
	}

	public int getLogLineIndex(String input)
	{
		Matcher matcher = PATTERN_LOG_SIGNATURE.matcher(input);

		int result = -1;

		if (!input.contains("made not entrant"))
		{
			if (matcher.find())
			{
				int count = matcher.groupCount();

				if (count >= 1)
				{
					result = matcher.group(1).length();
				}
			}
		}

		return result;
	}

	public int getWaitedTime(String line)
	{
		Matcher matcher = PATTERN_WAITED_SIGNATURE.matcher(line);

		int result = -1;

		if (matcher.find())
		{
			int count = matcher.groupCount();

			if (count == 3)
			{
				String group = matcher.group(2);

				try
				{
					result = Integer.parseInt(group);
				}
				catch (NumberFormatException nfe)
				{
					nfe.printStackTrace();
				}
			}
		}

		return result;
	}

	public int getCompileTime(String line)
	{
		Matcher matcher = PATTERN_COMPILE_TIME_SIGNATURE.matcher(line);

		int result = -1;

		if (matcher.find())
		{
			int count = matcher.groupCount();

			if (count == 3)
			{
				String group = matcher.group(2);

				try
				{
					result = Integer.parseInt(group);
				}
				catch (NumberFormatException nfe)
				{
					nfe.printStackTrace();
				}
			}
		}

		return result;
	}

	public ZingLine parseLine(String line)
	{
		ZingLine result = new ZingLine();

		try
		{
			String[] parts = line.split("\\s+");

			int pos = 0;

			String approxTS = parts[pos++];

			long timestamp = ParseUtil.parseStamp(approxTS);

			String compileID = parts[pos++];

			int compileIdValue = Integer.parseInt(compileID);

			result.setCompileId(compileIdValue);

			String flagsOrTier = parts[pos];

			int flagsOrTierLength = flagsOrTier.length();

			if (flagsOrTierLength == 1 && Character.isDigit(flagsOrTier.charAt(0)))
			{

			}
			else
			{
				for (int i = 0; i < flagsOrTierLength; i++)
				{
					char c = flagsOrTier.charAt(i);

					if (c == '!')
					{
						result.setThrowsExceptions(true);
					}
					else if (c == 's')
					{
					}
					else if (c == '%')
					{
					}
				}

				pos++;
			}

			String tier = parts[pos++];

			int tierValue = Integer.parseInt(tier);

			result.setTier(tierValue);

			if ("installed".equals(parts[pos]))
			{
				ZingLine queuedLine = compileIdMap.get(result.getCompileId());

				if (queuedLine == null)
				{
					logger.error("No queued compilation found for {}", result.getCompileId());
					result = null;
				}
				else
				{
					int waitedTime = getWaitedTime(line);

					// int compileTime = getCompileTime(line);

					result = queuedLine;

					result.setTimestampMillisNMethodEmitted(timestamp);

					result.setTimestampMillisQueued(result.getTimestampMillisCompileStart() - waitedTime);

					completeLineInstalled(result, parts, pos);
				}
			}
			else
			{
				result.setTimestampMillisCompileStart(timestamp);

				completeLineQueued(result, parts, pos);

				compileIdMap.put(result.getCompileId(), result);
			}
		}
		catch (Exception e)
		{
			logger.error("Bad line: {}", line, e);
			e.printStackTrace();
			result = null;
		}

		return result;
	}

	private void completeLineInstalled(ZingLine line, String[] parts, int pos) throws Exception
	{
		line.setLineType(ZingLineType.INSTALLED);

		pos++;
		pos++; // skip 'at';

		String startAddress = parts[pos++];
		long startAddressValue = ParseUtil.parseHexAddress(startAddress);

		line.setStartAddress(startAddressValue);

		pos++; // skip 'with'
		pos++; // skip 'size';

		String nativeSize = parts[pos++];

		int nativeSizeValue = (int) ParseUtil.parseHexAddress(nativeSize);

		line.setNativeSize(nativeSizeValue);
		line.setEndAddress(startAddressValue + nativeSizeValue);

		if ("from".equals(parts[pos]))
		{
			line.setStashedCompile(true);
		}
	}

	private void completeLineQueued(ZingLine line, String[] parts, int pos) throws Exception
	{
		line.setLineType(ZingLineType.QUEUED);

		String signature = parts[pos++];
		String signatureArgs = parts[pos++];

		line.setSignature(convertZingSigToLogCompilationSignature(signature + " " + signatureArgs));

		String maybeAtSign = parts[pos];

		if ("@".equals(maybeAtSign))
		{
			pos++; // @
			pos++; // bci
		}

		String score = parts[pos++];

		line.setScore(getIntValueFromStringStartingWithBracket(score));

		pos++; // 'score)'

		String bytecodeSize = parts[pos++];

		line.setBytecodeSize(getIntValueFromStringStartingWithBracket(bytecodeSize));
	}

	private int getIntValueFromStringStartingWithBracket(String input) throws Exception
	{
		input = input.substring(1, input.length());

		return Integer.parseInt(input);
	}

	private String convertZingSigToLogCompilationSignature(String zingSignature)
	{
		return zingSignature.replace("::", " ").replace(".", "/");
	}

	@Override
	protected void parseLogFile()
	{
		for (NumberedLine numberedLine : splitLog.getCompilationLines())
		{
			processLineNumber = numberedLine.getLineNumber();

			ZingLine zingLine = parseLine(numberedLine.getLine());

			if (zingLine == null)
			{
				continue;
			}

			if (DEBUG_LOGGING)
			{
				logger.debug("Zing log line parsed\n{}", zingLine);
			}

			switch (zingLine.getLineType())
			{
			case INSTALLED:
				Tag tagQueued = zingLine.toTagQueued();

				if (tagQueued != null)
				{
					handleTag(tagQueued);
				}

				Tag tagNMethod = zingLine.toTagNMethod();

				if (tagNMethod != null)
				{
					handleTag(tagNMethod);
				}

				Tag tagTask = zingLine.toTagTask();

				if (tagTask != null)
				{
					handleTag(tagTask);
				}

				break;
			default:
				break;

			}

		}
	}

	@Override
	protected void splitLogFile(File logFile)
	{
		reading = true;

		try (BufferedReader reader = new BufferedReader(new FileReader(logFile), 65536))
		{
			String currentLine = reader.readLine();

			while (reading && currentLine != null)
			{
				try
				{
					String trimmedLine = currentLine.trim();

					if (trimmedLine.contains("Compile Queue at VM exit"))
					{
						reading = false;
						break;
					}

					int logLineIndex = getLogLineIndex(trimmedLine);

					if (logLineIndex > 0)
					{
						trimmedLine = trimmedLine.substring(logLineIndex);
					}

					if (logLineIndex >= 0)
					{
						NumberedLine numberedLine = new NumberedLine(parseLineNumber++, trimmedLine);

						splitLog.addCompilationLine(numberedLine);
					}
				}
				catch (Exception ex)
				{
					logger.error("Exception handling: '{}'", currentLine, ex);
				}

				currentLine = reader.readLine();
			}
		}
		catch (IOException ioe)
		{
			logger.error("Exception while splitting log file", ioe);
		}
	}

	@Override
	protected void handleTag(Tag tag)
	{
		String tagName = tag.getName();

		if (DEBUG_LOGGING)
		{
			logger.debug("handling {}", tagName);
		}

		switch (tagName)
		{
		case TAG_TASK_QUEUED:
			handleTagQueued(tag);
			break;

		case TAG_NMETHOD:
			handleTagNMethod(tag);
			break;

		case TAG_TASK:
			handleTagTask((Task) tag);
			break;

		default:
			break;
		}
	}
}