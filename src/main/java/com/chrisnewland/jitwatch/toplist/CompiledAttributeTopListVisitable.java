/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.toplist;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;

public class CompiledAttributeTopListVisitable extends AbstractTopListVisitable
{
	private final String attr;

	public CompiledAttributeTopListVisitable(IReadOnlyJITDataModel model, String attr, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
		this.attr = attr;
	}

	@Override
	public void visit(IMetaMember mm)
	{
		if (mm.getCompiledAttribute(attr) != null)
		{
			long value = Long.valueOf(mm.getCompiledAttribute(attr));
			topList.add(new MemberScore(mm, value));
		}
	}
}
