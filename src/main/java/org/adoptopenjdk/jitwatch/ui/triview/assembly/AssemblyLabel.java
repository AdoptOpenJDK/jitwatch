/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview.assembly;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;

import javafx.scene.control.Label;

public class AssemblyLabel extends Label
{
	private AssemblyInstruction instruction;
	
	public AssemblyLabel(AssemblyInstruction instr, int annoWidth, int line)
	{
		super(instr.toString(annoWidth, line));
		instruction = instr;		
	}
	
	public AssemblyInstruction getInstruction()
	{
		return instruction;
	}
}