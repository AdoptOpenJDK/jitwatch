/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_BYTECODE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineTable
{
	private List<LineTableEntry> lineTableEntries = new ArrayList<>();

	private static final Logger logger = LoggerFactory.getLogger(LineTable.class);

	private MemberBytecode memberBytecode;

	public LineTable(MemberBytecode memberBytecode)
	{
		this.memberBytecode = memberBytecode;
	}

	public MemberBytecode getMemberBytecode()
	{
		return memberBytecode;
	}

	public void add(LineTableEntry entry)
	{
		lineTableEntries.add(entry);
		sort();
	}

	public void add(LineTable lineTable)
	{
		if (lineTable != null)
		{
			lineTableEntries.addAll(lineTable.lineTableEntries);
			sort();
		}
	}

	public int getLastSourceLine()
	{
		return getSourceRange()[1];
	}

	public int[] getSourceRange(int startBCI, int endBCI)
	{
		boolean first = true;

		int minSourceLine = 0;
		int maxSourceLine = 0;

		for (LineTableEntry entry : lineTableEntries)
		{
			int entryBCI = entry.getBytecodeOffset();
			int entrySourceLine = entry.getSourceOffset();

			if (entryBCI >= startBCI && entryBCI <= endBCI)
			{
				if (first)
				{
					minSourceLine = entrySourceLine;
					maxSourceLine = entrySourceLine;
					first = false;
				}
				else
				{
					minSourceLine = Math.min(entrySourceLine, minSourceLine);
					maxSourceLine = Math.max(entrySourceLine, maxSourceLine);
				}
			}
		}

		return new int[] { minSourceLine, maxSourceLine };
	}

	public int[] getSourceRange()
	{
		boolean first = true;

		int minSourceLine = 0;
		int maxSourceLine = 0;

		for (LineTableEntry entry : lineTableEntries)
		{
			int entrySourceLine = entry.getSourceOffset();

			if (first)
			{
				minSourceLine = entrySourceLine;
				maxSourceLine = entrySourceLine;
				first = false;
			}
			else
			{
				minSourceLine = Math.min(entrySourceLine, minSourceLine);
				maxSourceLine = Math.max(entrySourceLine, maxSourceLine);
			}
		}

		return new int[] { minSourceLine, maxSourceLine };
	}

	public boolean sourceLineInRange(int sourceLine)
	{
		boolean result = false;

		int[] sourceRange = getSourceRange();

		int minSourceLine = sourceRange[0];
		int maxSourceLine = sourceRange[1];

		result = (sourceLine >= minSourceLine) && (sourceLine <= maxSourceLine);

		if (DEBUG_LOGGING_BYTECODE)
		{
			logger.debug("{} in range {}-{} : {}", sourceLine, minSourceLine, maxSourceLine, result);
		}

		return result;
	}

	private void sort()
	{
		Collections.sort(lineTableEntries, new Comparator<LineTableEntry>()
		{
			@Override
			public int compare(LineTableEntry o1, LineTableEntry o2)
			{
				return Integer.compare(o1.getBytecodeOffset(), o2.getBytecodeOffset());
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

	public List<LineTableEntry> getEntries()
	{
		return Collections.unmodifiableList(lineTableEntries);
	}

	public int findSourceLineForBytecodeOffset(int searchBCI)
	{
		int sourceAtClosestBCI = -1;
		int distanceToClosestBCI = Integer.MAX_VALUE;

		for (LineTableEntry entry : lineTableEntries)
		{
			int entryBCI = entry.getBytecodeOffset();

			int distance = searchBCI - entryBCI;

			if (distance >= 0)
			{
				if (distance == 0)
				{
					sourceAtClosestBCI = entry.getSourceOffset();
					break;
				}
				else if (distance < distanceToClosestBCI)
				{
					distanceToClosestBCI = distance;
					sourceAtClosestBCI = entry.getSourceOffset();
				}
			}
		}

		return sourceAtClosestBCI;
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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lineTableEntries == null) ? 0 : lineTableEntries.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null)
		{
			return false;
		}

		if (getClass() != obj.getClass())
		{
			return false;
		}

		LineTable other = (LineTable) obj;

		if (lineTableEntries == null)
		{
			if (other.lineTableEntries != null)
			{
				return false;
			}
		}
		else if (!lineTableEntries.equals(other.lineTableEntries))
		{
			return false;
		}

		return true;
	}

}