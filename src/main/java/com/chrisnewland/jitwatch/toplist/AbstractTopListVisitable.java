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
	protected List<ITopListScore> topList;
	protected boolean sortHighToLow;

	public AbstractTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		this.model = model;
		this.sortHighToLow = sortHighToLow;
	}

	public void reset()
	{
	}

	//override if necessary
	public void postProcess()
	{
	}
	
	public List<ITopListScore> buildTopList()
	{
		topList = new ArrayList<>();

		TreeVisitor.walkTree(model, this);

		postProcess();
		
		Collections.sort(topList, new Comparator<ITopListScore>()
		{
			@Override
			public int compare(ITopListScore s1, ITopListScore s2)
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
