/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;

public class LineTableEntry
{
	private int sourceOffset;
	private int bytecodeOffset;

	public LineTableEntry(int sourceOffset, int bytecodeOffset)
	{
		this.sourceOffset = sourceOffset;
		this.bytecodeOffset = bytecodeOffset;
	}

	public int getSourceOffset()
	{
		return sourceOffset;
	}

	public int getBytecodeOffset()
	{
		return bytecodeOffset;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(sourceOffset).append(C_SPACE).append(C_COLON).append(C_SPACE);
		builder.append(bytecodeOffset);

		return builder.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + bytecodeOffset;
		result = prime * result + sourceOffset;
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

		LineTableEntry other = (LineTableEntry) obj;

		if (bytecodeOffset != other.bytecodeOffset)
		{
			return false;
		}

		if (sourceOffset != other.sourceOffset)
		{
			return false;
		}

		return true;
	}

}