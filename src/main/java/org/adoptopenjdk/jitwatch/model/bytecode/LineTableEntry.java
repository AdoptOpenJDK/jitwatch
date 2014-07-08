/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

public class LineTableEntry
{
	private String memberSignature;
	private int bytecodeOffset;

	public LineTableEntry(String memberSignature, int bytecodeOffset)
	{
		this.memberSignature = memberSignature;
		this.bytecodeOffset = bytecodeOffset;
	}

	public String getMemberSignature()
	{
		return memberSignature;
	}

	public int getBytecodeOffset()
	{
		return bytecodeOffset;
	}
}