/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview.bytecode;

import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.ui.triview.InstructionLabel;

public class BytecodeLabel extends InstructionLabel
{
	private BytecodeInstruction instruction;
	
	public BytecodeLabel(BytecodeInstruction instr, int maxOffset, int line)
	{
		super(instr.toString(maxOffset, line));
		instruction = instr;
	}
		
	public BytecodeInstruction getInstruction()
	{
		return instruction;
	}
}