/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import javafx.scene.paint.Color;

public class LineAnnotation
{	
	private String annotation;
	private Color colour;

	public LineAnnotation(String annotation, Color colour)
	{
		this.annotation = annotation;
		this.colour = colour;
	}

	public String getAnnotation()
	{
		return annotation;
	}

	public Color getColour()
	{
		return colour;
	}
}
