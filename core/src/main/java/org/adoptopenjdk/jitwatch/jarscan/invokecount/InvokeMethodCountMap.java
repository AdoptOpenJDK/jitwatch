/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.invokecount;

import java.util.EnumMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class InvokeMethodCountMap
{
	private Map<Opcode, MethodCountMap> opcodeMap = new EnumMap<>(Opcode.class);

	public void countInvocationOfMethod(Opcode opcode, String method)
	{
		MethodCountMap invokeCountMap = opcodeMap.get(opcode);

		if (invokeCountMap == null)
		{
			invokeCountMap = new MethodCountMap();
			opcodeMap.put(opcode, invokeCountMap);
		}

		invokeCountMap.count(method);
	}

	public String toString(int limitPerInvoke)
	{
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<Opcode, MethodCountMap> entry : opcodeMap.entrySet())
		{
			Opcode opcode = entry.getKey();
			MethodCountMap invokeCountMap = entry.getValue();
			
			builder.append(invokeCountMap.toString(opcode, limitPerInvoke));
		}

		return builder.toString();
	}
}