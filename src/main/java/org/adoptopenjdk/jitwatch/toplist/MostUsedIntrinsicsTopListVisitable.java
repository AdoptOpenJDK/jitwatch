/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.core.IntrinsicFinder;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.Journal;

public class MostUsedIntrinsicsTopListVisitable extends AbstractTopListVisitable
{
	private final Map<String, Integer> intrinsicountMap;

	public MostUsedIntrinsicsTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
		intrinsicountMap = new HashMap<>();
	}

	@Override
	public void visit(IMetaMember mm)
	{
		if (mm.isCompiled())
		{
			Journal journal = mm.getJournal();

			Map<String, String> intrinsicMap = IntrinsicFinder.findIntrinsics(journal);

			for (Map.Entry<String, String> entry : intrinsicMap.entrySet())
			{
				String iMapping = entry.getKey() + " => " + entry.getValue();

				int count = 0;

				if (intrinsicountMap.containsKey(iMapping))
				{
					count = intrinsicountMap.get(iMapping);
				}

				intrinsicountMap.put(iMapping, count + 1);
			}
		}
	}

	@Override
	public void postProcess()
	{
		for (Map.Entry<String, Integer> entry : intrinsicountMap.entrySet())
		{
			topList.add(new StringTopListScore(entry.getKey(), entry.getValue().longValue()));
		}
	}
}
