/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_MEMBER_CREATION;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.AbstractMetaMember;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;

public class HelperMetaMethod extends AbstractMetaMember
{
	public HelperMetaMethod(String methodName, MetaClass metaClass, Class<?>[] params, Class<?> returnType)
			throws NoSuchMethodException, SecurityException
	{
		super(methodName);

		Method dummyMethodObject = java.lang.String.class.getDeclaredMethod("length", new Class<?>[0]);

		this.metaClass = metaClass;

		this.returnType = returnType;
		this.paramTypes = Arrays.asList(params);

		// Can include non-method modifiers such as volatile so AND with
		// acceptable values
		this.modifier = dummyMethodObject.getModifiers() & Modifier.methodModifiers();

		this.isVarArgs = dummyMethodObject.isVarArgs();

		checkPolymorphicSignature(dummyMethodObject);

		metaClass.addMember(this);
		
		if (DEBUG_MEMBER_CREATION)
		{
			logger.debug("Created HelperMetaMethod: {}", toString());
		}
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