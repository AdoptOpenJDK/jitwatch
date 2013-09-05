package com.chrisnewland.jitwatch.meta;

import java.lang.reflect.Constructor;

public class MetaConstructor extends AbstractMetaMember implements Comparable<MetaConstructor>
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

	@Override
	public int compareTo(MetaConstructor o)
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