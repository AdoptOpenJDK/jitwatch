/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.histo;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;

public class NativeSizeHistoWalker extends AbstractHistoVisitable
{
	public NativeSizeHistoWalker(IReadOnlyJITDataModel model, long resolution)
	{
		super(model, resolution);
	}

	@Override
	public void visit(IMetaMember mm)
	{
		for (Compilation compilation : mm.getCompilations())
		{
			long nativeSize = compilation.getNativeSize();
	
			if (nativeSize != 0)
			{
				histo.addValue(nativeSize);
			}
		}
	}
}