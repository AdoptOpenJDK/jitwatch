/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.nextinstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NextInstructionCountList
{
	private List<NextInstructionCount> list = new ArrayList<>();

	public List<NextInstructionCount> getList()
	{
		Collections.sort(list, new Comparator<NextInstructionCount>()
		{
			@Override
			public int compare(NextInstructionCount o1, NextInstructionCount o2)
			{
				return Integer.compare(o2.getCount(), o1.getCount());
			}
		});

		return list;
	}

	public void add(NextInstructionCount nextBytecode)
	{
		list.add(nextBytecode);
	}
}
