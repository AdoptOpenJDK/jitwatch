/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.parser.j9;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_QUEUED;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.model.NumberedLine;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.parser.AbstractLogParser;

public class J9LogParser extends AbstractLogParser
{
	private int compileID = 1;
	private long timestampMillis = 0;

	public J9LogParser(IJITListener jitListener)
	{
		super(jitListener);
	}

	@Override
	protected void parseLogFile()
	{
		for (NumberedLine numberedLine : splitLog.getCompilationLines())
		{
			processLineNumber = numberedLine.getLineNumber();

			J9Line j9Line = J9Util.parseLine(numberedLine.getLine());
			
			if (DEBUG_LOGGING)
			{
				logger.debug("J9 log line parsed\n{}", j9Line);
			}
			
			Tag tagQueued = j9Line.toTagQueued(compileID, timestampMillis);
			Tag tagNMethod = j9Line.toTagNMethod(compileID, timestampMillis);
			Tag tagTask = j9Line.toTagTask(compileID, timestampMillis);
			
			compileID++;
			
			timestampMillis++;
			
			if (tagQueued != null)
			{
				handleTag(tagQueued);
			}
			
			if (tagNMethod != null)
			{
				handleTag(tagNMethod);
			}
			
			if (tagTask != null)
			{
				handleTag(tagTask);
			}
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

					if (trimmedLine.length() > 0)
					{
						char firstChar = trimmedLine.charAt(0);

						if (firstChar == '+')
						{
							NumberedLine numberedLine = new NumberedLine(parseLineNumber++, trimmedLine);

							splitLog.addCompilationLine(numberedLine);
						}
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
}