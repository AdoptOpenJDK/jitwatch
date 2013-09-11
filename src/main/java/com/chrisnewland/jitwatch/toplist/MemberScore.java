package com.chrisnewland.jitwatch.toplist;

import com.chrisnewland.jitwatch.model.IMetaMember;

public class MemberScore
{
	private final IMetaMember member;
	private final long score;

	public MemberScore(IMetaMember member, long score)
	{
		this.member = member;
		this.score = score;
	}
	
	public IMetaMember getMember()
	{
		return member;
	}

	public long getScore()
	{
		return score;
	}
}