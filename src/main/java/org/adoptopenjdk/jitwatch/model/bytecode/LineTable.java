/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class LineTable
{
	private List<LineTableEntry> lineTableEntries = new ArrayList<>();

	public void add(LineTableEntry entry)
	{
		lineTableEntries.add(entry);
	}
	
	public void add(LineTable lineTable)
	{
		if (lineTable != null)
		{
			lineTableEntries.addAll(lineTable.lineTableEntries);
		}
	}
	
	public void sort()
	{
		Collections.sort(lineTableEntries, new Comparator<LineTableEntry>()
		{
			@Override
			public int compare(LineTableEntry o1, LineTableEntry o2)
			{
				return Integer.compare(o1.getSourceOffset(), o2.getSourceOffset());
			}
		});
	}

	public LineTableEntry getEntryForSourceLine(int sourceLine)
	{
		LineTableEntry result = null;

		for (LineTableEntry entry : lineTableEntries)
		{
			if (entry.getSourceOffset() == sourceLine)
			{
				result = entry;
				break;
			}
		}

		return result;
	}

	public int findSourceLineForBytecodeOffset(int offset)
	{
		int result = -1;

		// LineNumberTable:
		// line 7: 0
		// line 8: 8
		// line 9: 32

		LineTableEntry previousEntry = null;

		for (LineTableEntry entry : lineTableEntries)
		{
			int currentBytecodeOffset = entry.getBytecodeOffset();

			if (offset == currentBytecodeOffset)
			{
				result = entry.getSourceOffset();
				break;
			}
			else if (offset < currentBytecodeOffset)
			{
				result = previousEntry.getSourceOffset();
				break;
			}

			previousEntry = entry;
		}

		// unmatched so return last offset
		if (result == -1)
		{
			result = previousEntry.getSourceOffset();
		}

		return result;
	}

	public int size()
	{
		return lineTableEntries.size();
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		for (LineTableEntry entry : lineTableEntries)
		{
			builder.append(entry).append(C_NEWLINE);
		}

		return builder.toString();
	}
}