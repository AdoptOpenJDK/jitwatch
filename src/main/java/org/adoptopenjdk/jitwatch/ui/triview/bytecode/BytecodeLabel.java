/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview.bytecode;

import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;

import javafx.scene.control.Label;

public class BytecodeLabel extends Label
{
	private BytecodeInstruction instruction;
	private String unhighlightedStyle;
	
	public BytecodeLabel(BytecodeInstruction instr, int maxOffset, int line)
	{
		setText(instr.toString(maxOffset, line));
		instruction = instr;
	}
	
	public void setUnhighlightedStyle(String style)
	{
		unhighlightedStyle = style;
		setStyle(unhighlightedStyle);
	}
	
	public String getUnhighlightedStyle()
	{
		return unhighlightedStyle;
	}
	
	public BytecodeInstruction getInstruction()
	{
		return instruction;
	}
}