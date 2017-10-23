/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import javafx.scene.control.Label;

public class InstructionLabel extends Label
{
	private String unhighlightedStyle = Viewer.STYLE_UNHIGHLIGHTED;
	
	public InstructionLabel(String text)
	{
		super(text);
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
}