/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.javap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.adoptopenjdk.jitwatch.process.AbstractProcess;

public class JavapProcess extends AbstractProcess
{
	private Path executablePath;

	private final String EXECUTABLE_NAME = "javap" + getExecutableSuffix();

	public JavapProcess(Path executablePath)
	{
		this.executablePath = executablePath;
	}

	public JavapProcess() throws FileNotFoundException
	{
		super();

		Path javaHome = Paths.get(System.getProperty("java.home"));
		
		executablePath = Paths.get(javaHome.toString(), "..", "bin", EXECUTABLE_NAME);

		if (!executablePath.toFile().exists())
		{
			executablePath = Paths.get(javaHome.toString(), "bin", EXECUTABLE_NAME);

			if (!executablePath.toFile().exists())
			{
				throw new FileNotFoundException("Could not find " + EXECUTABLE_NAME);
			}
		}

		executablePath = executablePath.normalize();
	}
	
	private List<String> getJavapCommands(Collection<String> classLocations, String fqClassName)
	{
		String[] args;

		if (classLocations == null || classLocations.isEmpty())
		{
			args = new String[] { "-c", "-p", "-v", fqClassName };
		}
		else
		{
			StringBuilder classPathBuilder = new StringBuilder();

			for (String cp : classLocations)
			{
				classPathBuilder.append(cp).append(File.pathSeparatorChar);
			}

			classPathBuilder.deleteCharAt(classPathBuilder.length() - 1);

			args = new String[] { "-c", "-p", "-v", "-classpath", classPathBuilder.toString(), fqClassName };
		}

		List<String> commands = new ArrayList<>();
		
		commands.add(executablePath.toString());
		commands.addAll(Arrays.asList(args));
		
		return commands;
	}
		
	public boolean execute(Collection<String> classLocations, String fqClassName)
			throws IOException
	{
		
		List<String> commands = getJavapCommands(classLocations, fqClassName);
		
		return runCommands(commands, null);
	}
}