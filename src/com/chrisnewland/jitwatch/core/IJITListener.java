package com.chrisnewland.jitwatch.core;

public interface IJITListener
{
	public void handleJITEvent(JITEvent event);
	public void handleLogEntry(String entry);
	public void handleErrorEntry(String entry);
}