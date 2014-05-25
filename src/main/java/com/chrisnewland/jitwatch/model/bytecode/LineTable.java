/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model.bytecode;

import java.util.HashMap;
import java.util.Map;

import com.chrisnewland.jitwatch.model.IMetaMember;

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
				if (lineEntry.getMemberSignature().equals(member.toString())
						|| lineEntry.getMemberSignature().equals(member.toStringUnqualifiedMethodName()))
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
}