/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.runtime;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_GROOVY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.process.AbstractProcess;

public class RuntimeGroovy extends AbstractProcess implements IRuntime
{
	private Path runtimePath;
	private Path groovyLibDir;

	private final String RUNTIME_NAME = "java" + getExecutableSuffix();

	public RuntimeGroovy(String languageHomeDir) throws FileNotFoundException
	{
		super();

		// Groovy is executed on the current running Java VM
		runtimePath = Paths.get(System.getProperty("java.home"), "bin", RUNTIME_NAME);

		groovyLibDir = Paths.get(languageHomeDir, "../lib");

		if (!runtimePath.toFile().exists())
		{
			throw new FileNotFoundException("Could not find " + RUNTIME_NAME);
		}

		runtimePath = runtimePath.normalize();
	}

	private void addGroovyJars(List<String> classpathEntries)
	{
		File libDir = groovyLibDir.toFile();

		String[] jars = libDir.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".jar");
			}
		});

		for (String jar : jars)
		{
			classpathEntries.add(new File(libDir, jar).getAbsolutePath());
		}
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

		commands.add("-cp");

		addGroovyJars(classpathEntries);

		commands.add(makeClassPath(classpathEntries));

		commands.add(className);

		return runCommands(commands, logListener);
	}

	@Override
	public String getClassToExecute(File fileToRun)
	{
		String filename = fileToRun.getName();
		return filename.substring(0, filename.length() - (VM_LANGUAGE_GROOVY.length() + 1));
	}

	@Override
	public String getClassForTriView(File fileToRun)
	{
		return getClassToExecute(fileToRun);
	}
}