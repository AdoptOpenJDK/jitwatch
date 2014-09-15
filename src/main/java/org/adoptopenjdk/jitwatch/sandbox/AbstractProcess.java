/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.sandbox;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;

import java.util.List;

import org.adoptopenjdk.jitwatch.sandbox.runtime.RuntimeJava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProcess implements IExternalProcess
{
	protected static final Logger logger = LoggerFactory.getLogger(RuntimeJava.class);

	protected StreamCollector outBuilder;
	protected StreamCollector errBuilder;
	
	@Override
	public String getOutputStream()
	{
		String result = null;
		
		if (outBuilder != null)
		{
			result = outBuilder.getStreamString();
		}
		
		return result;
	}
	
	@Override
	public String getErrorStream()
	{
		String result = null;
		
		if (errBuilder != null)
		{
			result = errBuilder.getStreamString();
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
			
			Process proc = pb.start();

			//TODO: how to not miss start of output?
			errBuilder = new StreamCollector(proc.getErrorStream());
			outBuilder = new StreamCollector(proc.getInputStream());

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