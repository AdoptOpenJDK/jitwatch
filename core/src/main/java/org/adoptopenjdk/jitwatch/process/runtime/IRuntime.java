/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.runtime;

import java.io.File;
import java.util.List;

import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.process.IExternalProcess;

public interface IRuntime extends IExternalProcess
{
	public boolean execute(String className, List<String> classpathEntries, List<String> vmOptions, ILogListener logListener);
	
	public String getClassToExecute(File fileToRun);
	
	public String getClassForTriView(File fileToRun);
}