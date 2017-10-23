/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview.assembly;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.ui.triview.InstructionLabel;

public class AssemblyLabel extends InstructionLabel
{
	private AssemblyInstruction instruction;
	
	public AssemblyLabel(AssemblyInstruction instr, int annoWidth, int line, boolean showLocalLabels)
	{
		super(instr.toString(annoWidth, line, showLocalLabels));
		instruction = instr;		
	}
	
	public AssemblyInstruction getInstruction()
	{
		return instruction;
	}
}