/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.model;

import java.lang.reflect.Constructor;

public class MetaConstructor extends AbstractMetaMember
{
	private String constructorToString;

	public MetaConstructor(Constructor<?> constructor, MetaClass methodClass)
	{
		this.constructorToString = constructor.toString();
		this.methodClass = methodClass;	
		
		memberName = constructor.getName();
		paramTypes = constructor.getParameterTypes();
		modifier = constructor.getModifiers();
	}

	@Override
	public String toString()
	{
		String methodSigWithoutThrows = constructorToString;

		int closingParentheses = methodSigWithoutThrows.indexOf(')');

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