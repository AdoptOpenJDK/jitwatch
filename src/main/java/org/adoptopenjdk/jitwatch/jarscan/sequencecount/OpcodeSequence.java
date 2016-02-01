/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.sequencecount;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class OpcodeSequence implements Comparable<OpcodeSequence>
{
	private List<Opcode> sequence = new ArrayList<>();

	public OpcodeSequence(List<Opcode> opcodeList)
	{
		sequence.addAll(opcodeList);
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		for (Opcode opcode : sequence)
		{
			builder.append(opcode.getMnemonic()).append("->");
		}

		builder.delete(builder.length() - 2, builder.length());

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
	public int compareTo(OpcodeSequence o)
	{
		return toString().compareTo(o.toString());
	}
}