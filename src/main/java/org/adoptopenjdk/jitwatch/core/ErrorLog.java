/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;

import java.util.Map;
import java.util.TreeMap;

public class ErrorLog
{
	private Map<String, Integer> errorCountMap = new TreeMap<>();

	public void clear()
	{
		errorCountMap.clear();
	}

	public void addEntry(String entry)
	{
		if (errorCountMap.containsKey(entry))
		{
			errorCountMap.put(entry, errorCountMap.get(entry) + 1);
		}
		else
		{
			errorCountMap.put(entry, 1);
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<String, Integer> entry : errorCountMap.entrySet())
		{
			String msg = entry.getKey();
			int count = entry.getValue();

			if (count == 1)
			{
				builder.append(msg).append(S_NEWLINE);
			}
			else
			{
				builder.append(msg).append(S_SPACE).append(S_OPEN_PARENTHESES).append(count).append(S_SPACE).append("times").append(S_CLOSE_PARENTHESES).append(S_NEWLINE);
			}
		}

		return builder.toString();

	}
}
