/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.suggestion;

import com.chrisnewland.jitwatch.model.IMetaMember;

public class Suggestion
{
	private final IMetaMember member;
	private final String suggestion;
	private final int score;
	
	public Suggestion(IMetaMember member, String suggestion, int score)
	{
		this.member = member;
		this.suggestion = suggestion;
		this.score = score;
	}

	public IMetaMember getMember()
	{
		return member;
	}

	public String getSuggestion()
	{
		return suggestion;
	}
	
	public int getScore()
	{
		return score;
	}
}
