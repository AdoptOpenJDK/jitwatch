/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.nextinstruction;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class NextInstructionCount
{
	private Opcode opcode;
	
	private int count;

	public NextInstructionCount(Opcode opcode, int count)
	{
		this.opcode = opcode;
		this.count = count;
	}

	public Opcode getOpcode()
	{
		return opcode;
	}

	public int getCount()
	{
		return count;
	}
}
