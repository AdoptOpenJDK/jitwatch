/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class BCParamConstant implements IBytecodeParam
{
	private int value;
	
	public BCParamConstant(String inConstant)
	{
		// remove leading # for constant pool param
		String constant = inConstant.substring(1);
		
		value = Integer.parseInt(constant);
	}
	
	@Override
	public String toString()
	{
		return S_HASH + Integer.toString(value);
	}

	@Override
	public Integer getValue()
	{
		return value;
	}	
}