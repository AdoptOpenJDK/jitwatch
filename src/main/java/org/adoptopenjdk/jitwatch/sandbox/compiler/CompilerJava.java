/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.sandbox.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adoptopenjdk.jitwatch.sandbox.AbstractProcess;
import org.adoptopenjdk.jitwatch.sandbox.ISandboxLogListener;

public class CompilerJava extends AbstractProcess implements ICompiler
{
	private String compilerPath;

	public CompilerJava(String compilerPath)
	{
		this.compilerPath = compilerPath;
	}

	@Override
	public boolean compile(List<File> sourceFiles, List<String> classpathEntries, File outputDir, ISandboxLogListener logListener)
			throws IOException
	{
		List<String> commands = new ArrayList<>();

		File javaCompiler = new File(compilerPath);

		String outputDirPath = outputDir.getAbsolutePath().toString();

		Set<String> uniqueCPSet = new HashSet<>(classpathEntries);
		uniqueCPSet.add(outputDirPath);

		commands.add(javaCompiler.getAbsolutePath());

		List<String> compileOptions = Arrays.asList(new String[] { "-g", "-d", outputDirPath });

		commands.addAll(compileOptions);

		commands.add("-cp");

		StringBuilder cpBuilder = new StringBuilder();

		for (String cp : uniqueCPSet)
		{
			cpBuilder.append(cp).append(File.pathSeparatorChar);
		}

		commands.add(cpBuilder.toString());

		for (File sourceFile : sourceFiles)
		{
			commands.add(sourceFile.getAbsolutePath());
		}

		return runCommands(commands, logListener);
	}
}