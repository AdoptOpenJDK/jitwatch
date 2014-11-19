/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.JITWatchConstants;

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

			return copy;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		synchronized (entryList)
		{
			for (Tag tag : entryList)
			{
				builder.append(tag.toString(true)).append(JITWatchConstants.C_NEWLINE);
			}
		}

		return builder.toString();
	}
}