/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.invokecounter;

import java.util.List;
import org.adoptopenjdk.jitwatch.jarscan.IJarScanOperation;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.adoptopenjdk.jitwatch.util.ParseUtil;

public class InvokeCounter implements IJarScanOperation
{
	private OpcodeInvokeCountMap opcodeInvokeCountMap;

	public InvokeCounter()
	{
		opcodeInvokeCountMap = new OpcodeInvokeCountMap();
	}

	@Override
	public String getReport()
	{
		return opcodeInvokeCountMap.toString();
	}
	
	private void count(String className, BytecodeInstruction instruction)
	{	
		//System.out.println(className + " == " + instruction.getComment());
		
		String comment = instruction.getCommentWithMemberPrefixStripped();
		
		String methodSig = ParseUtil.bytecodeMethodCommentToReadableString(className, comment);
	
		opcodeInvokeCountMap.count(instruction.getOpcode(), methodSig);
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
			case INVOKEDYNAMIC:
			case INVOKEINTERFACE:
			case INVOKESPECIAL:
			case INVOKESTATIC:
			case INVOKEVIRTUAL:
				count(className, instruction);
				break;
				
			default:
				break;
			}
		}

	}
}
