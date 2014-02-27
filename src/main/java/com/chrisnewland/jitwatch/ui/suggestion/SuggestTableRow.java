/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.suggestion;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.suggestion.Suggestion;

public class SuggestTableRow
{
	private final Suggestion suggestion;

	public SuggestTableRow(Suggestion suggestion)
	{
		this.suggestion = suggestion;
	}

	public IMetaMember getCaller()
	{
		return suggestion.getCaller();
	}

	public String getSuggestion()
	{
		return suggestion.getSuggestion();
	}

	public int getScore()
	{
		return suggestion.getScore();
	}

	public String getType()
	{

		switch (suggestion.getType())
		{
		case BRANCH:
			return "Branch";
		case INLINING:
			return "Inlinling";
		default:
			return "Unknown";
		}
	}
}