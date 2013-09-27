/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.core;

public interface IJITListener
{
	void handleJITEvent(JITEvent event);
	void handleLogEntry(String entry);
	void handleErrorEntry(String entry);
}