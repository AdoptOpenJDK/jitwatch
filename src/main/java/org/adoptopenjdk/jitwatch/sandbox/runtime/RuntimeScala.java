/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.sandbox.runtime;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOLLAR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_SCALA;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.sandbox.ISandboxLogListener;

public class RuntimeScala extends AbstractRuntime
{
	private String runtimePath;

	public RuntimeScala(String runtimePath)
	{
		this.runtimePath = runtimePath;
	}

	@Override
	public boolean execute(String className, List<String> classpathEntries, List<String> vmOptions, ISandboxLogListener logListener)
	{
		List<String> commands = new ArrayList<>();

		File javaExecutable = new File(runtimePath);

		commands.add(javaExecutable.getAbsolutePath());

		for (String vmOption : vmOptions)
		{
			commands.add(vmOption.replace("-XX:", "-J-XX:"));
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

	@Override
	public String getClassForTriView(File fileToRun)
	{
		// http://stackoverflow.com/questions/9350528/how-to-work-with-javap-for-scala-java-interoperability
		return getClassToExecute(fileToRun) + C_DOLLAR;
	}
}