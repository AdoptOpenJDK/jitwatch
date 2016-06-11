/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.suggestion;

import org.adoptopenjdk.jitwatch.suggestion.Suggestion;

public class SuggestTableRow
{
	private final Suggestion suggestion;

	public SuggestTableRow(Suggestion suggestion)
	{
		this.suggestion = suggestion;
	}

	public int getBytecodeOffset()
	{
		return suggestion.getBytecodeOffset();
	}

	public Suggestion getSuggestion()
	{
		return suggestion;
	}

	public String getText()
	{
		return suggestion.getText();
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
		case CODE_CACHE:
			return "Code Cache";
		default:
			return "Unknown";
		}
	}
}