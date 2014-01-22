/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import javafx.scene.paint.Color;

public class LineAnnotation
{
	public enum AnnotationType
	{
		SOURCE,BYTECODE,ASSEMBLY
	}
	
	private AnnotationType type;
	private String annotation;
	private Color colour;

	public LineAnnotation(AnnotationType type, String annotation, Color colour)
	{
		this.type = type;
		this.annotation = annotation;
		this.colour = colour;
	}

	public AnnotationType getType()
	{
		return type;
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
