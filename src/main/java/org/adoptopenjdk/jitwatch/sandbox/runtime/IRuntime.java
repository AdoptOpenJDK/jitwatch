/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.sandbox.runtime;

import java.io.File;
import java.util.List;

import org.adoptopenjdk.jitwatch.sandbox.ISandboxLogListener;

public interface IRuntime
{
	public boolean execute(String className, List<String> classpathEntries, List<String> vmOptions, ISandboxLogListener logListener);
	
	public String getClassToExecute(File fileToRun);
	
	public String getClassForTriView(File fileToRun);
}