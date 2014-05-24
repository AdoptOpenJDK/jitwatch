/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.triview.bytecode;

import com.chrisnewland.jitwatch.model.bytecode.BytecodeInstruction;

import javafx.scene.control.Label;

public class BytecodeLabel extends Label
{
	private BytecodeInstruction instruction;
	private String unhighlightedStyle;
	
	public BytecodeLabel(BytecodeInstruction instr, int maxOffset)
	{
		setText(instr.toString(maxOffset));
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