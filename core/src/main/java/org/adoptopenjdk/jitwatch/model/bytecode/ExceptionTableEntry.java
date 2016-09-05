/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

public class ExceptionTableEntry
{
	private int from;
	private int to;
	private int target;
	private String type;

	public int getFrom()
	{
		return from;
	}

	public int getTo()
	{
		return to;
	}

	public int getTarget()
	{
		return target;
	}

	public String getType()
	{
		return type;
	}

	public ExceptionTableEntry(int from, int to, int target, String type)
	{
		this.from = from;
		this.to = to;
		this.target = target;
		this.type = type;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append(from).append(" \t ");
		builder.append(to).append( "\t ");
		builder.append(target).append(" \t ");
		builder.append(type);
		
		return builder.toString();
	}
}