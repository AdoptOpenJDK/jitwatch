/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;

public class LineAnnotation
{
	private String annotation;
	private BCAnnotationType type;
	private Object metaData;

	public LineAnnotation(String annotation, BCAnnotationType type)
	{
		this(annotation, type, null);
	}

	public LineAnnotation(String annotation, BCAnnotationType type, Object metaData)
	{
		this.annotation = annotation;
		this.type = type;
		this.metaData = metaData;
	}

	public String getAnnotation()
	{
		return annotation;
	}

	public BCAnnotationType getType()
	{
		return type;
	}

	public Object getMetaData()
	{
		return metaData;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
		result = prime * result + ((metaData == null) ? 0 : metaData.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineAnnotation other = (LineAnnotation) obj;
		if (annotation == null)
		{
			if (other.annotation != null)
				return false;
		}
		else if (!annotation.equals(other.annotation))
			return false;

		if (metaData == null)
		{
			if (other.metaData != null)
				return false;
		}
		else if (!metaData.equals(other.metaData))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(type).append(S_SPACE).append(annotation);

		return builder.toString();
	}
}