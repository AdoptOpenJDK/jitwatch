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

import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.treevisitor.TreeVisitor;

public abstract class AbstractTopListVisitable implements ITopListVisitable
{
	protected IReadOnlyJITDataModel model;
	protected List<MemberScore> topList;
	protected boolean sortHighToLow;

	public AbstractTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		this.model = model;
		this.sortHighToLow = sortHighToLow;
	}

	public void reset()
	{
	}

	public List<MemberScore> buildTopList()
	{
		topList = new ArrayList<>();

		TreeVisitor.walkTree(model, this);

		Collections.sort(topList, new Comparator<MemberScore>()
		{
			@Override
			public int compare(MemberScore s1, MemberScore s2)
			{
				if (sortHighToLow)
				{
					return Long.compare(s2.getScore(), s1.getScore());
				}
				else
				{
					return Long.compare(s1.getScore(), s2.getScore());
				}

			}
		});

		return topList;
	}
}
