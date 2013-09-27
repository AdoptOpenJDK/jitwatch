/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.model;

import java.lang.reflect.Method;

public class MetaMethod extends AbstractMetaMember implements Comparable<MetaMethod>
{
	private String methodToString;

	public MetaMethod(Method method, MetaClass methodClass)
	{
		this.methodToString = method.toString();
		this.methodClass = methodClass;
		
		memberName = method.getName();
		returnType = method.getReturnType();		
		paramTypes = method.getParameterTypes();
		modifier = method.getModifiers();
	}

	@Override
	public String toString()
	{
		String methodSigWithoutThrows = methodToString;

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
		String ts = methodToString;

		int openParams = ts.lastIndexOf('(');

		if (openParams != -1)
		{
			int pos = openParams;

			int lastDot = -1;

			while (pos-- > 0)
			{
				if (ts.charAt(pos) == '.' && lastDot == -1)
				{
					lastDot = pos;
				}

				if (ts.charAt(pos) == ' ')
				{
					break;
				}
			}

			StringBuilder builder = new StringBuilder(ts);
			if (lastDot != -1)
			{
				builder.delete(pos + 1, lastDot + 1);
			}
			ts = builder.toString();

		}

		return ts;
	}

	@Override
	public int compareTo(MetaMethod o)
	{
		if (o == null)
		{
			return -1;
		}
		else
		{
			return toString().compareTo(o.toString());
		}
	}
}