/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class LineTableEntry
{
	private String memberSignature;
	private int sourceOffset;
	private int bytecodeOffset;

	public LineTableEntry(String memberSignature, int sourceOffset, int bytecodeOffset)
	{
		this.memberSignature = memberSignature;
		this.sourceOffset = sourceOffset;
		this.bytecodeOffset = bytecodeOffset;
	}

	public String getMemberSignature()
	{
		return memberSignature;
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
		
		builder.append(memberSignature).append(C_SPACE).append(C_COLON).append(C_SPACE);
		builder.append(sourceOffset).append(C_SPACE).append(C_COLON).append(C_SPACE);
		builder.append(bytecodeOffset);
		
		return builder.toString();
	}
}