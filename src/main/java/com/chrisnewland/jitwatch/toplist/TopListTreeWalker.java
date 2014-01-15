/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
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

public class TopListTreeWalker
{
	// Good case for J8 Streams
	public static List<MemberScore> buildTopListForAttribute(PackageManager pm, ITopListFilter filter)
	{
		List<MemberScore> topList = new ArrayList<>();

		List<MetaPackage> roots = pm.getRootPackages();

		for (MetaPackage mp : roots)
		{
			walkTree(mp, topList, filter);
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

	private static void walkTree(MetaPackage mp, List<MemberScore> topList, ITopListFilter filter)
	{
		List<MetaPackage> childPackages = mp.getChildPackages();

		for (MetaPackage childPackage : childPackages)
		{
			walkTree(childPackage, topList, filter);
		}

		List<MetaClass> packageClasses = mp.getPackageClasses();

		for (MetaClass mc : packageClasses)
		{
			for (IMetaMember mm : mc.getMetaMembers())
			{
				if (filter.acceptMember(mm))
				{
				    topList.add(filter.getScore(mm));
				}
			}
		}
	}
}
