/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;

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