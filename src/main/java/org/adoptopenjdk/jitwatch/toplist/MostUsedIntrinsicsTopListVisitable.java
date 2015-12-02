/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.intrinsic.IntrinsicFinder;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;

public class MostUsedIntrinsicsTopListVisitable extends AbstractTopListVisitable
{
	private final Map<String, Integer> intrinsicCountMap;

	public MostUsedIntrinsicsTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
		intrinsicCountMap = new HashMap<>();
	}

	@Override
	public void visit(IMetaMember metaMember)
	{
		if (metaMember.isCompiled())
		{
			IntrinsicFinder finder = new IntrinsicFinder();

			Map<String, String> intrinsicMap = finder.findIntrinsics(metaMember);

			for (Map.Entry<String, String> entry : intrinsicMap.entrySet())
			{
				String iMapping = entry.getKey() + " => " + entry.getValue();

				int count = 0;

				if (intrinsicCountMap.containsKey(iMapping))
				{
					count = intrinsicCountMap.get(iMapping);
				}

				intrinsicCountMap.put(iMapping, count + 1);
			}
		}
	}

	@Override
	public void postProcess()
	{
		for (Map.Entry<String, Integer> entry : intrinsicCountMap.entrySet())
		{
			topList.add(new StringTopListScore(entry.getKey(), entry.getValue().longValue()));
		}
	}
}