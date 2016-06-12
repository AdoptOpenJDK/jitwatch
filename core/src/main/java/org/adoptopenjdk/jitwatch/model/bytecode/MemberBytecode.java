/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_BYTECODE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberBytecode
{
	private List<BytecodeInstruction> bytecodeInstructions = new ArrayList<>();

	private LineTable lineTable;

	private MemberSignatureParts msp;

	private ClassBC classBytecode;

	private static final Logger logger = LoggerFactory.getLogger(MemberBytecode.class);
	
	private BytecodeAnnotations bytecodeAnnotations = new BytecodeAnnotations();

	public MemberBytecode(ClassBC classBytecode, MemberSignatureParts msp)
	{
		this.msp = msp;
		this.classBytecode = classBytecode;
		lineTable = new LineTable(this);
	}

	public ClassBC getClassBytecode()
	{
		return classBytecode;
	}
	
	public BytecodeAnnotations getBytecodeAnnotations()
	{
		return bytecodeAnnotations;
	}

	public MemberSignatureParts getMemberSignatureParts()
	{
		return msp;
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
		if (DEBUG_LOGGING_BYTECODE)
		{
			logger.debug("getBytecodeAtOffset({})", bci);
		}

		BytecodeInstruction result = null;

		for (BytecodeInstruction instruction : bytecodeInstructions)
		{
			if (DEBUG_LOGGING_BYTECODE)
			{
				logger.debug("checking: {}", instruction);
			}

			if (instruction.getOffset() == bci)
			{
				result = instruction;
				break;
			}
		}

		if (DEBUG_LOGGING_BYTECODE)
		{
			logger.debug("found: {}", result);
		}
		return result;
	}

	public void addLineTableEntry(LineTableEntry entry)
	{
		this.lineTable.add(entry);
	}

	public LineTable getLineTable()
	{
		return lineTable;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("MemberBytcode signature:\n").append(msp).append(S_NEWLINE);

		for (BytecodeInstruction instruction : bytecodeInstructions)
		{
			builder.append(instruction.toString()).append(S_NEWLINE);
		}

		return builder.toString();
	}
}