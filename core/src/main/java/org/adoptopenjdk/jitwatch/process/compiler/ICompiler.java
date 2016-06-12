/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.compiler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.process.IExternalProcess;

public interface ICompiler extends IExternalProcess
{
	public boolean compile(List<File> sourceFiles, List<String> classpathEntries, File outputDir, ILogListener logListener) throws IOException;
}