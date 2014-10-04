/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SplitLog
{
	private List<String> headerLines = new ArrayList<>();
	private List<String> classLoaderLines = new ArrayList<>();
	private List<String> logCompilationLines = new ArrayList<>();
	private List<String> assemblyLines = new ArrayList<>();
	
	public void clear()
	{
		headerLines.clear();
		classLoaderLines.clear();
		logCompilationLines.clear();
		assemblyLines.clear();
	}
	
	public void addHeaderLine(String line)
	{
		headerLines.add(line);
	}
	
	public void addClassLoaderLine(String line)
	{
		classLoaderLines.add(line);
	}
	
	public void addLogCompilationLine(String line)
	{
		logCompilationLines.add(line);
	}
	
	public void addAssemblyLine(String line)
	{
		assemblyLines.add(line);
	}

	public List<String> getHeaderLines()
	{
		return Collections.unmodifiableList(headerLines);
	}
	
	public List<String> getClassLoaderLines()
	{
		return Collections.unmodifiableList(classLoaderLines);
	}
	
	public List<String> getLogCompilationLines()
	{
		return Collections.unmodifiableList(logCompilationLines);
	}
	
	public List<String> getAssemblyLines()
	{
		return Collections.unmodifiableList(assemblyLines);
	}
}
