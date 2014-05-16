package com.chrisnewland.jitwatch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExecutionUtil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionUtil.class);

	public static boolean execute(String className, List<String> classpathEntries, List<String> vmOptions)
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
				cpBuilder.append(cp).append(":");
			}

			commands.add(cpBuilder.toString());
		}

		commands.add(className);
		
		//--------------------
		
		StringBuilder cmdBuilder = new StringBuilder();


		for (String part : commands)
		{
			cmdBuilder.append(part).append(" ");
		}
		
		LOGGER.info("Executing: {}", cmdBuilder.toString());

		int result = -1;
		
		try
		{
			ProcessBuilder pb = new ProcessBuilder(commands);
			
			Process proc = pb.start();

			//TODO: how to not miss start of output?
			StreamCollector outBuilder = new StreamCollector(proc.getInputStream());
			StreamCollector errBuilder = new StreamCollector(proc.getErrorStream());

			result = proc.waitFor();

			LOGGER.info("Execution complete: {}", result);
			LOGGER.info("Process out: {}", outBuilder.getStreamString());
			LOGGER.info("Process err: {}", errBuilder.getStreamString());
		}
		catch (Exception e)
		{
			LOGGER.error("Could not execute program", e);
		}

        // normal completion
		return result == 0;
	}
}
