/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import java.util.ArrayList;
import java.util.List;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;


public class ExceptionTable
{
	private List<ExceptionTableEntry> entries;

	public ExceptionTable()
	{
		entries = new ArrayList<>();
	}

	public void add(ExceptionTableEntry entry)
	{
		entries.add(entry);
	}

	public ExceptionTableEntry getEntryForBCI(int bci)
	{
		ExceptionTableEntry result = null;

		for (ExceptionTableEntry entry : entries)
		{
			if (bci >= entry.getFrom() && bci <= entry.getTo())
			{
				result = entry;
				break;
			}
		}

		return result;
	}

	public int size()
	{
		return entries.size();
	}

	public List<ExceptionTableEntry> getEntries()
	{
		return entries;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("from \t to \t target \t type\n");

		for (ExceptionTableEntry entry : entries)
		{
			builder.append(entry.toString()).append(S_NEWLINE);
		}

		return builder.toString();
	}
}