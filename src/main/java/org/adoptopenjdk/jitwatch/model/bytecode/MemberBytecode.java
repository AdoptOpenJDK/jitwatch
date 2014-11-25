/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.JITWatchConstants;

public class MemberBytecode
{
	private List<BytecodeInstruction> bytecodeInstructions = new ArrayList<>();

	private LineTable lineTable = new LineTable();

	public MemberBytecode()
	{
	}

	public void setInstructions(List<BytecodeInstruction> bytecodeInstructions)
	{
		this.bytecodeInstructions = bytecodeInstructions;
	}

	public List<BytecodeInstruction> getInstructions()
	{
		return bytecodeInstructions;
	}

	public BytecodeInstruction getBytecodeAtOffset(int bci)
	{
		BytecodeInstruction result = null;

		for (BytecodeInstruction instruction: bytecodeInstructions)
		{
			if (instruction.getOffset() == bci)
			{
				result = instruction;
				break;
			}
		}

		return result;
	}

	public void setLineTable(LineTable table)
	{
		this.lineTable = table;
	}

	public LineTable getLineTable()
	{
		return lineTable;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		for (BytecodeInstruction instruction: bytecodeInstructions)
		{
			builder.append(instruction.toString()).append(JITWatchConstants.S_NEWLINE);
		}

		return builder.toString();
	}
}