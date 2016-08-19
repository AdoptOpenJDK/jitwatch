/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.compilation;

import java.util.HashSet;
import java.util.Set;

import org.adoptopenjdk.jitwatch.model.Tag;

public abstract class AbstractCompilationVisitable implements ICompilationVisitable
{	
	protected Set<String> ignoreTags = new HashSet<>();

	protected void handleOther(Tag tag)
	{		
		if (!ignoreTags.contains(tag.getName()))
		{
			CompilationUtil.unhandledTag(this, tag);
		}
	}
}