/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

public class MemberScore implements ITopListScore
{
	private final IMetaMember member;
	private final long score;

	public MemberScore(IMetaMember member, long score)
	{
		this.member = member;
		this.score = score;
	}
	
	@Override
	public IMetaMember getKey()
	{
		return member;
	}

	@Override
	public long getScore()
	{
		return score;
	}
}