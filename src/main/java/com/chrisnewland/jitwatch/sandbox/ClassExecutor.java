package com.chrisnewland.jitwatch.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.C_COLON;
import static com.chrisnewland.jitwatch.core.JITWatchConstants.C_SPACE;

public class ClassExecutor
{
	private static final Logger logger = LoggerFactory.getLogger(ClassExecutor.class);

    private StreamCollector outBuilder;
    private StreamCollector errBuilder;

    public boolean execute(String className, List<String> classpathEntries, List<String> vmOptions)
	{
		List<String> commands = new ArrayList<>();

		commands.add("java");

		if (vmOptions.size() > 0)
		{
			commands.addAll(vmOptions);
		}

		if (classpathEntries.size() > 0)
		{
			commands.add("-cp");

			StringBuilder cpBuilder = new StringBuilder();

			for (String cp : classpathEntries)
			{
				cpBuilder.append(cp).append(C_COLON);
			}

			commands.add(cpBuilder.toString());
		}

		commands.add(className);
				
		StringBuilder cmdBuilder = new StringBuilder();


		for (String part : commands)
		{
			cmdBuilder.append(part).append(C_SPACE);
		}
		
		logger.info("Executing: {}", cmdBuilder.toString());

		int result = -1;
		
		try
		{
			ProcessBuilder pb = new ProcessBuilder(commands);
			
			Process proc = pb.start();

			//TODO: how to not miss start of output?
			outBuilder = new StreamCollector(proc.getInputStream());
			errBuilder = new StreamCollector(proc.getErrorStream());

			result = proc.waitFor();
		}
		catch (Exception e)
		{
			logger.error("Could not execute program", e);
		}
		
		return result == 0; // normal completion
	}
	
	public String getOutputStream()
	{
		String result = null;
		
		if (outBuilder != null)
		{
			result = outBuilder.getStreamString();
		}
		
		return result;
	}
	
	public String getErrorStream()
	{
		String result = null;
		
		if (errBuilder != null)
		{
			result = errBuilder.getStreamString();
		}
		
		return result;
	}
}
