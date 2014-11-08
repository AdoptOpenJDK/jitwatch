/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import org.adoptopenjdk.jitwatch.suggestion.Suggestion;

public interface ITriView
{
	void highlightBytecodeForSuggestion(Suggestion suggestion);
	void highlightSourceLine(int line);
}