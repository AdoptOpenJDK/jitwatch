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
	
	private ExceptionTable exceptionTable;

	private MemberSignatureParts msp;

	private ClassBC classBytecode;
	
	private int size = 0;

	private static final Logger logger = LoggerFactory.getLogger(MemberBytecode.class);
	
	private BytecodeAnnotations bytecodeAnnotations = new BytecodeAnnotations();

	public MemberBytecode(ClassBC classBytecode, MemberSignatureParts msp)
	{
		this.msp = msp;
		this.classBytecode = classBytecode;
		lineTable = new LineTable(this);
		exceptionTable = new ExceptionTable();
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
		
		if (!bytecodeInstructions.isEmpty())
		{
			BytecodeInstruction instruction = bytecodeInstructions.get(bytecodeInstructions.size() -1);
			
			if (instruction != null)
			{
				int bci = instruction.getOffset();
				
				size = bci + 1;
			}
		}
	}

	public int size()
	{
		return size;
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
		lineTable.add(entry);
	}

	public LineTable getLineTable()
	{
		return lineTable;
	}
	
	public void addExceptionTableEntry(ExceptionTableEntry entry)
	{
		exceptionTable.add(entry);
	}

	public ExceptionTable getExceptionTable()
	{
		return exceptionTable;
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