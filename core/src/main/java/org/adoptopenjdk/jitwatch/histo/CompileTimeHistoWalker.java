/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.histo;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;

public class CompileTimeHistoWalker extends AbstractHistoVisitable
{
	public CompileTimeHistoWalker(IReadOnlyJITDataModel model, long resolution) // TODO filter by compile level?
	{
		super(model, resolution);
	}

	@Override
	public void visit(IMetaMember mm)
	{
		for (Compilation compilation : mm.getCompilations())
		{
			if (!compilation.isC2N())
			{
				histo.addValue(compilation.getCompileTime());
			}
		}
	}
}