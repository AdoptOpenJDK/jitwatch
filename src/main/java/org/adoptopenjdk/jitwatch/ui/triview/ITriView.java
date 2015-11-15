/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import org.adoptopenjdk.jitwatch.suggestion.Suggestion;

public interface ITriView
{
	public static final int MASK_UPDATE_NONE = 0;
	public static final int MASK_UPDATE_SOURCE = 1;
	public static final int MASK_UPDATE_BYTECODE = 2;
	public static final int MASK_UPDATE_ASSEMBLY = 4;

	void highlightBytecodeForSuggestion(Suggestion suggestion);
	void highlightBytecodeOffset(int bci, int updateMask);
	void highlightSourceLine(int line, int updateMask);
}