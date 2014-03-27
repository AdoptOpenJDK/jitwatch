/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model.bytecode;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class BCParamConstant implements IBytecodeParam
{
	private int value;
	
	public BCParamConstant(String constant)
	{
		// remove leading # for constant pool param
		constant = constant.substring(1);
		
		value = Integer.parseInt(constant);
	}
	
	@Override
	public String toString()
	{
		return S_HASH + Integer.toString(value);
	}

	@Override
	public Object getValue()
	{
		return value;
	}	
}