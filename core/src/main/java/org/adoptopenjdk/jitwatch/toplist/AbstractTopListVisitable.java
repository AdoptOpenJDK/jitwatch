/*
 * Copyright (c) 2013-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.adoptopenjdk.jitwatch.compilation.AbstractCompilationVisitable;
import org.adoptopenjdk.jitwatch.model.*;
import org.adoptopenjdk.jitwatch.treevisitor.TreeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTopListVisitable extends AbstractCompilationVisitable implements ITopListVisitable
{
    protected IReadOnlyJITDataModel model;
    protected List<ITopListScore> topList;
    protected boolean sortHighToLow;

    protected static final Logger logger = LoggerFactory.getLogger(AbstractTopListVisitable.class);

	public AbstractTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		this.model = model;
		this.sortHighToLow = sortHighToLow;
		topList = new ArrayList<>();
	}

	protected abstract void postProcess();

	@Override
	public List<ITopListScore> buildTopList()
	{
		topList.clear();

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
