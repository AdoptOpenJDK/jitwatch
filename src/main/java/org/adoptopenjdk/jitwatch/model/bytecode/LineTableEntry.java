/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;

public class LineTableEntry
{
	private MemberSignatureParts msp;
	private int sourceOffset;
	private int bytecodeOffset;

	public LineTableEntry(MemberSignatureParts msp, int sourceOffset, int bytecodeOffset)
	{
		this.msp = msp;
		this.sourceOffset = sourceOffset;
		this.bytecodeOffset = bytecodeOffset;
	}

	public MemberSignatureParts getMemberSignatureParts()
	{
		return msp;
	}
	
	public int getSourceOffset()
	{
		return sourceOffset;
	}

	public int getBytecodeOffset()
	{
		return bytecodeOffset;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append(msp.toString()).append(C_SPACE).append(C_COLON).append(C_SPACE);
		builder.append(sourceOffset).append(C_SPACE).append(C_COLON).append(C_SPACE);
		builder.append(bytecodeOffset);
		
		return builder.toString();
	}
}