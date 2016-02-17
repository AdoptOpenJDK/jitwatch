/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.sequencesearch;

import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;

public class FoundSequence
{
	private int startingBCI;

	private MemberSignatureParts msp;

	public FoundSequence(int startingBCI, MemberSignatureParts msp)
	{
		this.startingBCI = startingBCI;
		this.msp = msp;
	}

	public int getStartingBCI()
	{
		return startingBCI;
	}

	public MemberSignatureParts getMemberSignatureParts()
	{
		return msp;
	}

	@Override
	public String toString()
	{
		return msp.toStringSingleLine() + " : " + startingBCI;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((msp == null) ? 0 : msp.hashCode());
		result = prime * result + startingBCI;
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

		FoundSequence other = (FoundSequence) obj;

		if (msp == null)
		{
			if (other.msp != null)
			{
				return false;
			}
		}
		else if (!msp.equals(other.msp))
		{
			return false;
		}
		
		if (startingBCI != other.startingBCI)
		{
			return false;
		}
		
		return true;
	}
}