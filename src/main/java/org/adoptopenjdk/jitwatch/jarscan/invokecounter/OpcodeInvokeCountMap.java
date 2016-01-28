/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.invokecounter;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.EnumMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class OpcodeInvokeCountMap
{
	private Map<Opcode, InvokeCountMap> opcodeMap = new EnumMap<>(Opcode.class);

	public void count(Opcode opcode, String method)
	{
		InvokeCountMap invokeCountMap = opcodeMap.get(opcode);

		if (invokeCountMap == null)
		{
			invokeCountMap = new InvokeCountMap();
			opcodeMap.put(opcode, invokeCountMap);
		}

		invokeCountMap.count(method);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<Opcode, InvokeCountMap> entry : opcodeMap.entrySet())
		{
			Opcode opcode = entry.getKey();
			InvokeCountMap invokeCountMap = entry.getValue();
			
			builder.append(invokeCountMap.toString(opcode)).append(S_NEWLINE);
		}

		return builder.toString();
	}
}