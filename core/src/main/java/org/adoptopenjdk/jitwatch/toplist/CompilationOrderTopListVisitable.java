/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import org.adoptopenjdk.jitwatch.model.*;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class CompilationOrderTopListVisitable extends AbstractTopListVisitable
{
	public CompilationOrderTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
	}

	@Override public void reset()
	{
	}

	@Override public void postProcess()
	{
	}

	@Override public void visitTag(Compilation compilation, Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
	}

	@Override public void visit(IMetaMember metaMember)
	{
		String compileID = metaMember.getCompiledAttribute(ATTR_COMPILE_ID);

		String compileKind = metaMember.getCompiledAttribute(ATTR_COMPILE_KIND);

		if (compileID != null && (compileKind == null || !OSR.equals(compileKind)))
		{
			long value = Long.valueOf(metaMember.getCompiledAttribute(ATTR_COMPILE_ID));

			topList.add(new MemberScore(metaMember, value));
		}
	}
}