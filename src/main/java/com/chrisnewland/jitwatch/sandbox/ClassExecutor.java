package com.chrisnewland.jitwatch.sandbox;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class ClassExecutor
{
	private static final Logger logger = LoggerFactory.getLogger(ClassExecutor.class);

	private StreamCollector outBuilder;
	private StreamCollector errBuilder;
	
	public boolean execute(String className, List<String> classpathEntries, List<String> vmOptions)
	{
		List<String> commands = new ArrayList<>();

		// locate currently running java executable
		Path pathToJavaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java");

		File javaExecutable = pathToJavaExecutable.toFile();
		
		commands.add(javaExecutable.getAbsolutePath());

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
			
			// use this instead of StreamCollectors if missing start of output
			//pb.redirectErrorStream(true);
			//pb.redirectOutput(Redirect.INHERIT);
			
			Process proc = pb.start();

			//TODO: how to not miss start of output?
			errBuilder = new StreamCollector(proc.getErrorStream());
			outBuilder = new StreamCollector(proc.getInputStream());

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
