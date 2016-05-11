/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.process.AbstractProcess;

public class CompilerKotlin extends AbstractProcess implements ICompiler
{
	private Path compilerPath;

	private final String COMPILER_NAME = "kotlinc-jvm";
	public static final String KOTLIN_EXECUTABLE_JAR = "jitwatch-sandbox-kotlin.jar";

	public CompilerKotlin(String languageHomeDir) throws FileNotFoundException
	{
		super();

		compilerPath = Paths.get(languageHomeDir, "bin", COMPILER_NAME);

		if (!compilerPath.toFile().exists())
		{
			throw new FileNotFoundException("Could not find " + COMPILER_NAME);
		}

		compilerPath = compilerPath.normalize();
	}

	@Override
	public boolean compile(List<File> sourceFiles, List<String> classpathEntries, File outputDir, ILogListener logListener)
			throws IOException
	{
		List<String> commands = new ArrayList<>();

		commands.add(compilerPath.toString());

		String outputDirPath = outputDir.getAbsolutePath().toString();

		List<String> compileOptions = Arrays.asList(new String[] {
				"-include-runtime",
				"-d",
				outputDirPath + File.separator + KOTLIN_EXECUTABLE_JAR });

		commands.addAll(compileOptions);

		if (classpathEntries.size() > 0)
		{
			commands.add("-classpath");
			commands.add(makeClassPath(classpathEntries));
		}

		for (File sourceFile : sourceFiles)
		{
			commands.add(sourceFile.getAbsolutePath());
		}

		return runCommands(commands, logListener);
	}
}