/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;

public class AnnotationException extends Exception
{
	private static final long serialVersionUID = 1L;

	private int bytecodeOffset;
	private BytecodeInstruction instruction;

	public AnnotationException(String msg, int offset, BytecodeInstruction instruction)
	{
		super(msg);
		this.bytecodeOffset = offset;
		this.instruction = instruction;
	}

	public int getBytecodeOffset()
	{
		return bytecodeOffset;
	}

	public BytecodeInstruction getInstruction()
	{
		return instruction;
	}

	@Override
	public String getMessage()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(super.getMessage());
		builder.append(" at offset: ").append(bytecodeOffset).append(C_SPACE);
		builder.append("but was mnemonic: ");

		if (instruction != null && instruction.getOpcode() != null)
		{
			builder.append(instruction.getOpcode().getMnemonic());
		}
		else
		{
			builder.append("Unknown instruction");
		}

		return builder.toString();
	}
}
