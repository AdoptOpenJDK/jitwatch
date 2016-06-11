/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;

public class LineAnnotation
{
	private String annotation;
	private BCAnnotationType type;

	public LineAnnotation(String annotation, BCAnnotationType type)
	{
		this.annotation = annotation;
		this.type = type;
	}

	public String getAnnotation()
	{
		return annotation;
	}

	public BCAnnotationType getType()
	{
		return type;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(type).append(S_SPACE).append(annotation);

		return builder.toString();
	}
}
