/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.sequencecount;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;

public class InstructionSequence implements Comparable<InstructionSequence>
{
	private List<Opcode> sequence = new ArrayList<>();

	public InstructionSequence(List<Opcode> opcodeList)
	{
		sequence.addAll(opcodeList);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		for (Opcode opcode : sequence)
		{
			builder.append(opcode.getMnemonic()).append(S_COMMA);
		}

		builder.delete(builder.length() - 1, builder.length());

		return builder.toString();
	}

	public Opcode getOpcodeAtIndex(int index)
	{
		return sequence.get(index);
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return toString().equals(obj.toString());
	}

	@Override
	public int compareTo(InstructionSequence o)
	{
		return toString().compareTo(o.toString());
	}
}