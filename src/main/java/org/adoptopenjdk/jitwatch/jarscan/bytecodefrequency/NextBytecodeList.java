/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.bytecodefrequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NextBytecodeList
{
	private List<NextBytecode> list = new ArrayList<>();

	private int sum = 0;

	public List<NextBytecode> getList()
	{
		Collections.sort(list, new Comparator<NextBytecode>()
		{
			@Override
			public int compare(NextBytecode o1, NextBytecode o2)
			{
				return Integer.compare(o2.getCount(), o1.getCount());
			}
		});

		return list;
	}

	public int getSum()
	{
		return sum;
	}

	public void add(NextBytecode nextBytecode)
	{
		list.add(nextBytecode);

		sum += nextBytecode.getCount();
	}
}
