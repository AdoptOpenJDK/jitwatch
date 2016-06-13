/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.runtime;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.adoptopenjdk.jitwatch.loader.ResourceLoader;
import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.process.AbstractProcess;
import org.adoptopenjdk.jitwatch.process.compiler.CompilerKotlin;

public class RuntimeKotlin extends AbstractProcess implements IRuntime
{
	private Path runtimePath;

	//TODO how did this used to work?
	private final Path pathToRuntimeJar = Paths.get(CompilerKotlin.KOTLIN_EXECUTABLE_JAR);

	private final String RUNTIME_NAME = "java" + getExecutableSuffix();

	public RuntimeKotlin(String languageHomeDir) throws FileNotFoundException
	{
		super();

		// Kotlin is executed on the current running Java VM
		runtimePath = Paths.get(System.getProperty("java.home"), "bin", RUNTIME_NAME);

		if (!runtimePath.toFile().exists())
		{
			throw new FileNotFoundException("Could not find " + RUNTIME_NAME);
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

		commands.add("-jar");
		commands.add(pathToRuntimeJar.toString());

		return runCommands(commands, logListener);
	}

	@Override
	public String getClassToExecute(File fileToRun)
	{
		// Main class is in the jar manifest
		return S_EMPTY;
	}

	@Override
	public String getClassForTriView(File fileToRun)
	{
		Properties manifest = ResourceLoader.readManifestFromZip(pathToRuntimeJar.toFile());

		String mainClass = manifest.getProperty("Main-Class");

		return mainClass;
	}
}