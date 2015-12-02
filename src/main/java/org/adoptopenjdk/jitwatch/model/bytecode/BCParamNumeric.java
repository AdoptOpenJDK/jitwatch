/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

public class BCParamNumeric implements IBytecodeParam
{
	private int value;

	public BCParamNumeric(int value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return Integer.toString(value);
	}
	
	@Override
	public Integer getValue()
	{
		return value;
	}
}