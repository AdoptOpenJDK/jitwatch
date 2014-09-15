/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.sandbox.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.sandbox.ISandboxLogListener;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class RuntimeScala extends AbstractRuntime
{	
	private String runtimePath;
	
	public RuntimeScala(String runtimePath)
	{
		this.runtimePath = runtimePath;
	}
	
	public boolean execute(String className, List<String> classpathEntries, List<String> vmOptions, ISandboxLogListener logListener)
	{
		List<String> commands = new ArrayList<>();

		File javaExecutable = new File(runtimePath);
		
		commands.add(javaExecutable.getAbsolutePath());

		for (String vmOption : vmOptions)
		{
			commands.add("-J" + vmOption);
		}

		if (classpathEntries.size() > 0)
		{
			commands.add("-cp");

			StringBuilder cpBuilder = new StringBuilder();

			for (String cp : classpathEntries)
			{
				cpBuilder.append(cp).append(File.pathSeparatorChar);
			}

			commands.add(cpBuilder.toString());
		}

		commands.add(className);
				
		return runCommands(commands, logListener);
	}

	@Override
	public String getClassToExecute(File fileToRun)
	{
		String filename = fileToRun.getName();
		return filename.substring(0, filename.length() - (VM_LANGUAGE_SCALA.length() + 1));
	}
}