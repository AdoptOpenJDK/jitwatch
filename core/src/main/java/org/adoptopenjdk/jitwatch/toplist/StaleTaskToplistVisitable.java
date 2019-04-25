/*
 * Copyright (c) 2016-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.compilation.CompilationUtil;
import org.adoptopenjdk.jitwatch.model.*;

public class StaleTaskToplistVisitable extends AbstractTopListVisitable
{
	private final Map<IMetaMember, Integer> staleCompilationCountMap;

	public StaleTaskToplistVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
		staleCompilationCountMap = new HashMap<>();
	}

	@Override public void reset()
	{
		staleCompilationCountMap.clear();
	}

	@Override public void visitTag(Compilation compilation, Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
	}

	@Override public void visit(IMetaMember metaMember)
	{
		if (metaMember != null && metaMember.isCompiled())
		{

			for (Compilation compilation : metaMember.getCompilations())
			{
				Task taskTag = compilation.getTagTask();

				if (taskTag != null && CompilationUtil.isStaleTask(taskTag))
				{
					if (staleCompilationCountMap.containsKey(metaMember))
					{
						int count = staleCompilationCountMap.get(metaMember);
						staleCompilationCountMap.put(metaMember, count + 1);
					}
					else
					{
						staleCompilationCountMap.put(metaMember, 1);
					}
				}
			}
		}
	}

	@Override public void postProcess()
	{
		for (Map.Entry<IMetaMember, Integer> entry : staleCompilationCountMap.entrySet())
		{
			topList.add(new MemberScore(entry.getKey(), entry.getValue().longValue()));
		}
	}
}