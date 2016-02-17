/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.instructioncount;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COMMA;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.jarscan.IJarScanOperation;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class InstructionCountOperation implements IJarScanOperation
{
	private Map<Opcode, Integer> opcodeCountMap;

	private int limit;

	public InstructionCountOperation(int limit)
	{
		opcodeCountMap = new EnumMap<>(Opcode.class);
		this.limit = limit;
	}

	@Override
	public String getReport()
	{
		StringBuilder builder = new StringBuilder();
		
		List<Map.Entry<Opcode, Integer>> sortedList = new ArrayList<>(opcodeCountMap.entrySet());

		Collections.sort(sortedList, new Comparator<Map.Entry<Opcode, Integer>>()
		{
			@Override
			public int compare(Map.Entry<Opcode, Integer> o1, Map.Entry<Opcode, Integer> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		int outputCount = 0;

		for (Map.Entry<Opcode, Integer> entry : sortedList)
		{
			Opcode opcode = entry.getKey();
			Integer count = entry.getValue();
			
			builder.append(opcode.getMnemonic()).append(C_COMMA);
			builder.append(count).append(S_NEWLINE);
			
			outputCount++;;
			
			if (limit != 0 && outputCount == limit)
			{
				break;
			}
		}
				
		return builder.toString();
	}

	private void count(Opcode opcode)
	{
		Integer count = opcodeCountMap.get(opcode);
		
		if (count == null)
		{
			count = new Integer(1);
		}
		else
		{
			count++;
		}
		
		opcodeCountMap.put(opcode, count);
	}

	@Override
	public void processInstructions(String className, MemberBytecode memberBytecode)
	{
		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		for (BytecodeInstruction instruction : instructions)
		{
			Opcode opcode = instruction.getOpcode();
		
			count(opcode);
		}
	}
}