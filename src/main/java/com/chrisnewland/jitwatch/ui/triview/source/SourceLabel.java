/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.triview.source;

import com.chrisnewland.jitwatch.model.assembly.AssemblyInstruction;

import javafx.scene.control.Label;

public class SourceLabel extends Label
{
	private AssemblyInstruction instruction;
	
	public void setInstruction(AssemblyInstruction instr)
	{
		instruction = instr;
	}
	
	public AssemblyInstruction getInstruction()
	{
		return instruction;
	}
}