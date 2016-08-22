/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;

public class CompileTimeTopListVisitable extends AbstractTopListVisitable
{
	public CompileTimeTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
	}

	@Override
	public void visit(IMetaMember mm)
	{
		for (Compilation compilation : mm.getCompilations())
		{
			if (!compilation.isC2N())
			{
				topList.add(new MemberScore(mm, compilation.getCompileTime()));
			}
		}	
	}
}