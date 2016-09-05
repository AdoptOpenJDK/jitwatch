/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.hotthrow;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

public class HotThrowResult
{
	private IMetaMember member;
	private int bci;
	private String exceptionType;
	private boolean preallocated;
	
	public HotThrowResult(IMetaMember member, int bci, String exceptionType, boolean preallocated)
	{
		super();
		this.member = member;
		this.bci = bci;
		this.exceptionType = exceptionType;
		this.preallocated = preallocated;
	}

	public IMetaMember getMember()
	{
		return member;
	}

	public int getBci()
	{
		return bci;
	}

	public String getExceptionType()
	{
		return exceptionType;
	}

	public boolean isPreallocated()
	{
		return preallocated;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + bci;
		result = prime * result + ((exceptionType == null) ? 0 : exceptionType.hashCode());
		result = prime * result + ((member == null) ? 0 : member.hashCode());
		result = prime * result + (preallocated ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HotThrowResult other = (HotThrowResult) obj;
		if (bci != other.bci)
			return false;
		if (exceptionType == null)
		{
			if (other.exceptionType != null)
				return false;
		}
		else if (!exceptionType.equals(other.exceptionType))
			return false;
		if (member == null)
		{
			if (other.member != null)
				return false;
		}
		else if (!member.equals(other.member))
			return false;
		if (preallocated != other.preallocated)
			return false;
		return true;
	}

}
