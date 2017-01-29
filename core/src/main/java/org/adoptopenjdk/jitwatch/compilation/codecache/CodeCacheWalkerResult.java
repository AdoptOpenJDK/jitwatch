/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.compilation.codecache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.CodeCacheEvent;

public class CodeCacheWalkerResult
{
	private List<CodeCacheEvent> events;

	private long lowestAddress;

	private long highestAddress;

	public CodeCacheWalkerResult()
	{
		events = new ArrayList<>();
	}

	public void reset()
	{
		events.clear();
	}

	public void addEvent(CodeCacheEvent event)
	{
		long nativeCodeStart = event.getNativeAddress();
		long nativeCodeEnd = nativeCodeStart + event.getNativeCodeSize();

		if (events.size() == 0)
		{
			lowestAddress = nativeCodeStart;
			highestAddress = nativeCodeEnd;
		}
		else
		{
			lowestAddress = Math.min(nativeCodeStart, lowestAddress);
			highestAddress = Math.max(nativeCodeEnd, highestAddress);
		}

		events.add(event);
	}

	public List<CodeCacheEvent> getEvents()
	{
		Collections.sort(events, new Comparator<CodeCacheEvent>()
		{
			@Override
			public int compare(CodeCacheEvent o1, CodeCacheEvent o2)
			{
				return Long.compare(o1.getStamp(), o2.getStamp());
			}
		});

		return events;
	}

	public long getLowestAddress()
	{
		return lowestAddress;
	}

	public long getHighestAddress()
	{
		return highestAddress;
	}
}