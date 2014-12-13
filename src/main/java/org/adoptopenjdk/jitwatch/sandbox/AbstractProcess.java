/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.sandbox;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.adoptopenjdk.jitwatch.sandbox.runtime.RuntimeJava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProcess
{
	protected static final Logger logger = LoggerFactory.getLogger(RuntimeJava.class);

	public static final Path PATH_STD_ERR = new File(Sandbox.SANDBOX_DIR.toFile(), "sandbox.err").toPath();
	public static final Path PATH_STD_OUT = new File(Sandbox.SANDBOX_DIR.toFile(), "sandbox.out").toPath();

	public static String getOutputStream()
	{
		String result = null;

		if (PATH_STD_OUT.toFile().exists())
		{
			try
			{
				result = new String(Files.readAllBytes(PATH_STD_OUT), StandardCharsets.UTF_8);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}

		return result;
	}

	public static String getErrorStream()
	{
		String result = null;

		if (PATH_STD_ERR.toFile().exists())
		{
			try
			{
				result = new String(Files.readAllBytes(PATH_STD_ERR), StandardCharsets.UTF_8);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}

		return result;
	}

	protected boolean runCommands(List<String> commands, ISandboxLogListener logListener)
	{
		StringBuilder cmdBuilder = new StringBuilder();

		for (String part : commands)
		{
			cmdBuilder.append(part).append(C_SPACE);
		}

		logListener.log("Executing: " + cmdBuilder.toString());

		int result = -1;

		try
		{
			ProcessBuilder pb = new ProcessBuilder(commands);

			pb.redirectError(PATH_STD_ERR.toFile());
			pb.redirectOutput(PATH_STD_OUT.toFile());

			Process proc = pb.start();

			result = proc.waitFor();
		}
		catch (Exception e)
		{
			logListener.log("Could not run compiler:" + e);
			logger.error("Could not run compiler:", e);
		}

		return result == 0; // normal completion
	}
}