/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.logger;

public class StdLogListener implements ILogListener
{

	@Override
	public void handleLogEntry(String entry)
	{
		System.out.println(entry);
	}

	@Override
	public void handleErrorEntry(String entry)
	{		
		System.err.println(entry);
	}
}