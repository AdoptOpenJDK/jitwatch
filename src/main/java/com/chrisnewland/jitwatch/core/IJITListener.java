package com.chrisnewland.jitwatch.core;

public interface IJITListener
{
	void handleJITEvent(JITEvent event);
	void handleLogEntry(String entry);
	void handleErrorEntry(String entry);
}