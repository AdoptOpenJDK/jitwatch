/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.compiler;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.process.AbstractProcess;

public class CompilerJRuby extends AbstractProcess implements ICompiler
{
	// TODO this is broken. Not sure if possible with ProcessBuilder
	// http://blog.headius.com/2013/06/the-pain-of-broken-subprocess.html

	private Path compilerPath;

	private final String COMPILER_NAME = "jrubyc" + (isWindows() ? ".bat" : S_EMPTY);

	public CompilerJRuby(String languageHomeDir) throws FileNotFoundException
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

		String outputDirPath = outputDir.getAbsolutePath().toString();

		Set<String> uniqueCPSet = new HashSet<>(classpathEntries);
		uniqueCPSet.add(outputDirPath);

		commands.add(compilerPath.toString());

		// List<String> compileOptions = Arrays.asList(new String[] { "-g",
		// "-d", outputDirPath });
		//
		// commands.addAll(compileOptions);
		//

		if (classpathEntries.size() > 0)
		{
			commands.add("-cp");

			commands.add(makeClassPath(classpathEntries));
		}

		for (File sourceFile : sourceFiles)
		{
			commands.add(sourceFile.getAbsolutePath());
		}

		return runCommands(commands, logListener);
	}
}