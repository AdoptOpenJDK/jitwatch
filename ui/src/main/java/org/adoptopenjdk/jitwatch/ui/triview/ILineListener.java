/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

public interface ILineListener
{
	public enum LineType { SOURCE, BYTECODE, BYTECODE_BCI, ASSEMBLY, PLAIN }
	
	void lineHighlighted(int index, LineType lineType);
	
	void handleFocusSelf(LineType lineType);
	void handleFocusNext();
	void handleFocusPrev();
}