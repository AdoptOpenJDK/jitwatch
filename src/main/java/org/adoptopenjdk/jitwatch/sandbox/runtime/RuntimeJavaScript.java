/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.sandbox.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.process.AbstractProcess;
import org.adoptopenjdk.jitwatch.sandbox.ISandboxLogListener;
import org.adoptopenjdk.jitwatch.sandbox.Sandbox;

public class RuntimeJavaScript extends AbstractProcess implements IRuntime
{
	private Path runtimePath;

	private final String RUNTIME_NAME = "jjs";

	public RuntimeJavaScript(String languageHomeDir) throws FileNotFoundException
	{
		super(Sandbox.PATH_STD_ERR, Sandbox.PATH_STD_OUT);

		runtimePath = Paths.get(languageHomeDir, "bin", RUNTIME_NAME);

		if (!runtimePath.toFile().exists())
		{
			throw new FileNotFoundException("Could not find " + RUNTIME_NAME);
		}

		runtimePath = runtimePath.normalize();
	}

	@Override
	public boolean execute(String className, List<String> classpathEntries, List<String> vmOptions, ISandboxLogListener logListener)
	{
		List<String> commands = new ArrayList<>();

		commands.add(runtimePath.toString());
		
		// TODO support optimistic typing shortcuts
		commands.add("-ot=false"); 

		for (String vmOption : vmOptions)
		{
			commands.add(vmOption.replace("-XX:", "-J-XX:"));
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
		return fileToRun.getAbsolutePath();
	}

	@Override
	public String getClassForTriView(File fileToRun)
	{
		return "jdk.nashorn.internal.scripts.Script$run";
	}
}