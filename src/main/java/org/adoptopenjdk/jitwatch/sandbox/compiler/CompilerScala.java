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
import java.util.List;

import org.adoptopenjdk.jitwatch.sandbox.ISandboxLogListener;

public class CompilerScala extends AbstractCompiler
{
	private String compilerPath;
	
	public CompilerScala(String compilerPath)
	{
		this.compilerPath = compilerPath;
	}
	
	@Override
	public boolean compile(List<File> sourceFiles, List<String> classpathEntries, File outputDir, ISandboxLogListener logListener) throws IOException
	{
		List<String> commands = new ArrayList<>();

		File javaCompiler = new File(compilerPath);
		
		commands.add(javaCompiler.getAbsolutePath());

		List<String> compileOptions = Arrays.asList(new String[] { "-g:vars", "-d", outputDir.getAbsolutePath().toString() });

		commands.addAll(compileOptions);
		
		if (classpathEntries.size() > 0)
		{
			commands.add("-classpath");

			StringBuilder cpBuilder = new StringBuilder();

			for (String cp : classpathEntries)
			{
				cpBuilder.append(cp).append(File.pathSeparatorChar);
			}

			commands.add(cpBuilder.toString());
		}

		for (File sourceFile : sourceFiles)
		{
			commands.add(sourceFile.getAbsolutePath());
		}		
		
		return runCommands(commands, logListener);
	}
}