/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import java.util.ArrayList;
import java.util.List;

public class MemberBytecode
{	
	private List<BytecodeInstruction> bytecodeInstructions = new ArrayList<>();
	
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
}