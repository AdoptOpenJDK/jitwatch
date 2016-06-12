/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.adoptopenjdk.jitwatch.util.ParseUtil;

public class Journal
{
	// writes dominate so not COWAL
	private List<Tag> entryList;

	public Journal()
	{
		entryList = new ArrayList<>();
	}

	public void addEntry(Tag entry)
	{
		synchronized (entryList)
		{
			entryList.add(entry);
		}
	}

	public List<Tag> getEntryList()
	{
		synchronized (entryList)
		{
			List<Tag> copy = new ArrayList<>(entryList);

			Collections.sort(copy, new Comparator<Tag>()
			{
				@Override
				public int compare(Tag tag1, Tag tag2)
				{
					long ts1 = ParseUtil.getStamp(tag1.getAttributes());
					long ts2 = ParseUtil.getStamp(tag2.getAttributes());
					
					return Long.compare(ts1, ts2);
				}
			});

			return copy;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		for (Tag tag : getEntryList())
		{
			builder.append(tag.toString(true)).append(C_NEWLINE);
		}

		return builder.toString();
	}
}