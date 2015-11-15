/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

public class NumberedLine
{
	private long lineNumber;
	private String line;

	public long getLineNumber()
	{
		return lineNumber;
	}

	public void setLineNumber(long lineNumber)
	{
		this.lineNumber = lineNumber;
	}

	public String getLine()
	{
		return line;
	}

	public void setLine(String line)
	{
		this.line = line;
	}

	public NumberedLine(long lineNumber, String line)
	{
		this.lineNumber = lineNumber;
		this.line = line;
	}
}