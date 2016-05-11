/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.runtime;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_JAVA;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.process.AbstractProcess;

public class RuntimeJava extends AbstractProcess implements IRuntime
{
	private Path runtimePath;

	private final String RUNTIME_NAME = "java" + getExecutableSuffix();

	public RuntimeJava(String languageHomeDir) throws FileNotFoundException
	{
		super();

		runtimePath = Paths.get(languageHomeDir, "..", "bin", RUNTIME_NAME);

		if (!runtimePath.toFile().exists())
		{
			runtimePath = Paths.get(languageHomeDir, "bin", RUNTIME_NAME);

			if (!runtimePath.toFile().exists())
			{
				throw new FileNotFoundException("Could not find " + RUNTIME_NAME);
			}
		}

		runtimePath = runtimePath.normalize();
	}

	@Override
	public boolean execute(String className, List<String> classpathEntries, List<String> vmOptions, ILogListener logListener)
	{
		List<String> commands = new ArrayList<>();

		commands.add(runtimePath.toString());

		if (vmOptions.size() > 0)
		{
			commands.addAll(vmOptions);
		}

		if (classpathEntries.size() > 0)
		{
			commands.add("-cp");

			commands.add(makeClassPath(classpathEntries));
		}

		commands.add(className);

		return runCommands(commands, logListener);
	}

	@Override
	public String getClassToExecute(File fileToRun)
	{
		String filename = fileToRun.getName();
		return filename.substring(0, filename.length() - (VM_LANGUAGE_JAVA.length() + 1));
	}

	@Override
	public String getClassForTriView(File fileToRun)
	{
		return getClassToExecute(fileToRun);
	}
}