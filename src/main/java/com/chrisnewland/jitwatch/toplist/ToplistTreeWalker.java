/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.toplist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.MetaPackage;
import com.chrisnewland.jitwatch.model.PackageManager;

public class ToplistTreeWalker
{
	// Good case for J8 Streams
	public static List<MemberScore> buildTopListForAttribute(PackageManager pm, boolean compileAttribute, String attributeName)
	{
		List<MemberScore> topList = new ArrayList<>();

		List<MetaPackage> roots = pm.getRootPackages();

		for (MetaPackage mp : roots)
		{
			walkTree(mp, topList, compileAttribute, attributeName);
		}
		
		Collections.sort(topList, new Comparator<MemberScore>()
		{
			@Override
			public int compare(MemberScore s1, MemberScore s2)
			{
				// largest first
				return Long.compare(s2.getScore(), s1.getScore());
			}
		});

		return topList;
	}

	private static void walkTree(MetaPackage mp, List<MemberScore> topList, boolean isCompileAttribute, String attributeName)
	{
		List<MetaPackage> childPackages = mp.getChildPackages();

		for (MetaPackage childPackage : childPackages)
		{
			walkTree(childPackage, topList, isCompileAttribute, attributeName);
		}

		List<MetaClass> packageClasses = mp.getPackageClasses();

		for (MetaClass mc : packageClasses)
		{
			for (IMetaMember mm : mc.getMetaMembers())
			{
				String attrValue = null;

				if (isCompileAttribute)
				{
					attrValue = mm.getCompiledAttribute(attributeName);
				}
				else
				{
					attrValue = mm.getQueuedAttribute(attributeName);
				}

				if (attrValue != null)
				{
					long val = Long.valueOf(attrValue);

					topList.add(new MemberScore(mm, val));
				}
			}
		}
	}
}
