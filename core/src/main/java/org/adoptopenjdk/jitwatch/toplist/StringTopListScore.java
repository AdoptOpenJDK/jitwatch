/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

public class StringTopListScore implements ITopListScore
{
	private final String key;
	private final long score;

	public StringTopListScore(String key, long score)
	{
		this.key = key;
		this.score = score;
	}
	
	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public long getScore()
	{
		return score;
	}
}