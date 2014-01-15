/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.core;

public interface IJITListener
{
	void handleJITEvent(JITEvent event);
	void handleLogEntry(String entry);
	void handleErrorEntry(String entry);
}