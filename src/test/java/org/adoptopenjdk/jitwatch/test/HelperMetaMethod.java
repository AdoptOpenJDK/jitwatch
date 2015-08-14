/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import java.lang.reflect.Method;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.MetaMethod;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;

public class HelperMetaMethod extends MetaMethod
{
	public HelperMetaMethod(Method method, MetaClass methodClass)
	{
		super(method, methodClass);
	}
	
	private List<BytecodeInstruction> instructions;

	public void setInstructions(List<BytecodeInstruction> instructions)
	{
		this.instructions = instructions;
	}

	public List<BytecodeInstruction> getInstructions()
	{
		return instructions;
	}
}