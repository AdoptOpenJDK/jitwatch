/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.suggestion;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.suggestion.Suggestion;

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