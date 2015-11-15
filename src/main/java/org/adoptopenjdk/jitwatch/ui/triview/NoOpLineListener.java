/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

public class NoOpLineListener implements ILineListener
{
	@Override
	public void lineHighlighted(int index, LineType lineType)
	{
	}

	@Override
	public void handleFocusSelf(LineType lineType)
	{
	}

	@Override
	public void handleFocusNext()
	{
	}

	@Override
	public void handleFocusPrev()
	{
	}
}