/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.process.runtime.RuntimeJava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProcess implements IExternalProcess
{
	protected static final Logger logger = LoggerFactory.getLogger(RuntimeJava.class);

	private Path stdErr;
	private Path stdOut;
	
	public AbstractProcess()
	{
		try
		{
			stdErr = Files.createTempFile("stream", ".err");
			stdOut = Files.createTempFile("stream", ".out");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public String getExecutableSuffix()
	{
		return isWindows() ? ".exe" : "";
	}

	public String getLaunchScriptSuffix()
	{
		return isWindows() ? ".bat" : ".sh";
	}

	public boolean isWindows()
	{
		return System.getProperty("os.name", S_EMPTY).contains("Windows");
	}

	@Override
	public String getOutputStream()
	{
		String result = null;

		if (stdOut.toFile().exists())
		{
			try
			{
				result = new String(Files.readAllBytes(stdOut), StandardCharsets.UTF_8);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}

		return result;
	}

	@Override
	public String getErrorStream()
	{
		String result = null;

		if (stdErr.toFile().exists())
		{
			try
			{
				result = new String(Files.readAllBytes(stdErr), StandardCharsets.UTF_8);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}

		return result;
	}

	protected String makeClassPath(List<String> classpathEntries)
	{
		StringBuilder cpBuilder = new StringBuilder();

		for (String cp : classpathEntries)
		{
			cpBuilder.append(cp).append(File.pathSeparatorChar);
		}

		if (cpBuilder.length() > 0)
		{
			cpBuilder.deleteCharAt(cpBuilder.length() - 1);
		}

		return cpBuilder.toString();
	}

	protected boolean runCommands(List<String> commands, ILogListener logListener)
	{
		return runCommands(commands, null, null, logListener);
	}

	protected boolean runCommands(List<String> commands, File workingDirectory, Map<String, String> environment,
			ILogListener logListener)
	{
		StringBuilder cmdBuilder = new StringBuilder();

		for (String part : commands)
		{
			cmdBuilder.append(part).append(C_SPACE);
		}

		if (logListener != null)
		{
			logListener.handleLogEntry("Running: " + cmdBuilder.toString());
		}
		
		int result = -1;

		try
		{
			ProcessBuilder pb = new ProcessBuilder(commands);

			if (environment != null)
			{
				Map<String, String> processEnvironment = pb.environment();

				for (Map.Entry<String, String> entry : environment.entrySet())
				{
					processEnvironment.put(entry.getKey(), entry.getValue());
				}
			}

			if (workingDirectory != null)
			{
				pb.directory(workingDirectory);
			}

			pb.redirectError(stdErr.toFile());
			pb.redirectOutput(stdOut.toFile());

			Process proc = pb.start();

			result = proc.waitFor();
		}
		catch (Exception e)
		{
			if (logListener != null)
			{
				logListener.handleErrorEntry("Could not run external process:" + e);
			}
			
			logger.error("Could not run external process:", e);
		}

		return result == 0; // normal completion
	}
}