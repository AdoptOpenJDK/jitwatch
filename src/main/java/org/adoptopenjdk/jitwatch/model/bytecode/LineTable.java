/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class LineTable
{
	private Map<Integer, LineTableEntry> lineMap = new HashMap<>();

	public void put(int sourceLine, LineTableEntry entry)
	{
		lineMap.put(sourceLine, entry);
	}

	public LineTableEntry get(int sourceLine)
	{
		LineTableEntry result = null;

		if (lineMap.containsKey(sourceLine))
		{
			result = lineMap.get(sourceLine);
		}

		return result;
	}

	public int findSourceLine(IMetaMember member, int offset)
	{
		int result = -1;

		for (Map.Entry<Integer, LineTableEntry> entry : lineMap.entrySet())
		{
			LineTableEntry lineEntry = entry.getValue();

			if (lineEntry.getBytecodeOffset() == offset)
			{
				if (member.matchesBytecodeSignature(lineEntry.getMemberSignature()))
				{
					result = entry.getKey();
				}
			}
		}

		return result;
	}

	public int size()
	{
		return lineMap.size();
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		for (Map.Entry<Integer, LineTableEntry> entry : lineMap.entrySet())
		{
			LineTableEntry lineEntry = entry.getValue();

			builder.append(entry.getKey()).append(C_SPACE).append(lineEntry.getBytecodeOffset()).append(C_NEWLINE);
		}
		
		return builder.toString();
	}
}