/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.allocation;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOUBLE_QUOTE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import java.util.List;

import org.adoptopenjdk.jitwatch.jarscan.IJarScanOperation;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.IBytecodeParam;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class AllocationCountOperation implements IJarScanOperation
{
	private OpcodeAllocCountMap opcodeAllocCountMap;

	private int limitPerAllocOpcode;

	public AllocationCountOperation(int limit)
	{
		opcodeAllocCountMap = new OpcodeAllocCountMap();
		this.limitPerAllocOpcode = limit;
	}

	@Override
	public String getReport()
	{
		return opcodeAllocCountMap.toString(limitPerAllocOpcode);
	}

	private void count(Opcode opcode, String type)
	{
		opcodeAllocCountMap.count(opcode, type);
	}

	@Override
	public void processInstructions(String className, MemberBytecode memberBytecode)
	{
		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		for (BytecodeInstruction instruction : instructions)
		{
			Opcode opcode = instruction.getOpcode();
			
			switch (opcode)
			{
			case NEWARRAY:
			{
				List<IBytecodeParam> params = instruction.getParameters();
				String type = params.get(0).toString();
				count(opcode, type);
			}
				break;

			case ANEWARRAY:
			case NEW:
			case MULTIANEWARRAY:
			{
				String comment = instruction.getComment();
				String type = comment.substring("// class ".length(), comment.length());
				type = type.replace(S_DOUBLE_QUOTE, S_EMPTY);
				count(opcode, type);
			}
				break;

			default:
				break;
			}
		}
	}
}