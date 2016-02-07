/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.allocationcount;

import java.util.EnumMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class InstructionAllocCountMap
{
	private Map<Opcode, AllocCountMap> opcodeMap = new EnumMap<>(Opcode.class);

	public void count(Opcode opcode, String allocatedType)
	{
		AllocCountMap typeCountMap = opcodeMap.get(opcode);

		if (typeCountMap == null)
		{
			typeCountMap = new AllocCountMap();
			opcodeMap.put(opcode, typeCountMap);
		}

		typeCountMap.countAllocationOfType(allocatedType);
	}

	public String toString(int limitPerInvoke)
	{
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<Opcode, AllocCountMap> entry : opcodeMap.entrySet())
		{
			Opcode opcode = entry.getKey();
			AllocCountMap typeCountMap = entry.getValue();
			
			builder.append(typeCountMap.toString(opcode, limitPerInvoke));
		}

		return builder.toString();
	}
}