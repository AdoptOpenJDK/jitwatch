/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.suggestion.Suggestion;

public class SuggestTableRow
{
	private final Suggestion suggestion;

	public SuggestTableRow(Suggestion suggestion)
	{
		this.suggestion = suggestion;
	}

	public MetaClass getMetaClass()
	{
		return suggestion.getMember().getMetaClass();
	}

	public IMetaMember getMember()
	{
		return suggestion.getMember();
	}

	public String getSuggestion()
	{
		return suggestion.getSuggestion();
	}
}