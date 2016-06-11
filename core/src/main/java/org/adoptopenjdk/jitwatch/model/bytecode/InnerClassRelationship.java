/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InnerClassRelationship
{
	private String parentClass;
	private String childClass;
	
	private static final Pattern PATTERN = Pattern.compile("^.*class (.*) of class (.*)");

	
	private InnerClassRelationship()
	{
	}
	
	public static InnerClassRelationship parse(String line)
	{
		InnerClassRelationship result = null;
		
		Matcher matcher = PATTERN.matcher(line);

		if (matcher.find())
		{
			result = new InnerClassRelationship();
			
			result.childClass = (matcher.group(1) == null) ? null : matcher.group(1).replace(S_SLASH, S_DOT);
			result.parentClass = (matcher.group(2) == null) ? null : matcher.group(2).replace(S_SLASH, S_DOT);
		}

		return result;		
	}

	public String getParentClass()
	{
		return parentClass;
	}

	public String getChildClass()
	{
		return childClass;
	}

	@Override
	public String toString()
	{
		return "InnerClassRelationship [parentClass=" + parentClass + ", childClass=" + childClass + "]";
	}
}