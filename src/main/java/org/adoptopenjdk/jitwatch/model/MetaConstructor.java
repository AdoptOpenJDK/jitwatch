/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.adoptopenjdk.jitwatch.core.JITWatchConstants;

public class MetaConstructor extends AbstractMetaMember
{
	private String constructorToString;

	public MetaConstructor(Constructor<?> constructor, MetaClass methodClass)
	{
		this.constructorToString = constructor.toString();
		this.methodClass = methodClass;	
		
		returnType = Void.TYPE;
		memberName = constructor.getName();
		paramTypes = Arrays.asList(constructor.getParameterTypes());
		modifier = constructor.getModifiers();
	}

	@Override
	public String toString()
	{
		String methodSigWithoutThrows = constructorToString;

		int closingParentheses = methodSigWithoutThrows.indexOf(JITWatchConstants.S_CLOSE_PARENTHESES);

		if (closingParentheses != methodSigWithoutThrows.length() - 1)
		{
			methodSigWithoutThrows = methodSigWithoutThrows.substring(0, closingParentheses + 1);
		}

		return methodSigWithoutThrows;
	}

	@Override
	public String getSignatureForBytecode()
	{
		return constructorToString;
	}
}