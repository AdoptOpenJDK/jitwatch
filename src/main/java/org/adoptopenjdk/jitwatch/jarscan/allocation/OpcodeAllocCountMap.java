/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.allocation;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.EnumMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class OpcodeAllocCountMap
{
	private Map<Opcode, AllocCountMap> opcodeMap = new EnumMap<>(Opcode.class);

	public void count(Opcode opcode, String method)
	{
		AllocCountMap typeCountMap = opcodeMap.get(opcode);

		if (typeCountMap == null)
		{
			typeCountMap = new AllocCountMap();
			opcodeMap.put(opcode, typeCountMap);
		}

		typeCountMap.count(method);
	}

	public String toString(int limitPerInvoke)
	{
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<Opcode, AllocCountMap> entry : opcodeMap.entrySet())
		{
			Opcode opcode = entry.getKey();
			AllocCountMap typeCountMap = entry.getValue();
			
			builder.append(typeCountMap.toString(opcode, limitPerInvoke)).append(S_NEWLINE);
		}

		return builder.toString();
	}
}