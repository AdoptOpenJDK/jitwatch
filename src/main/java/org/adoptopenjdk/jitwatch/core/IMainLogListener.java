package org.adoptopenjdk.jitwatch.core;

public interface IMainLogListener
{
	void handleLogEntry(String entry);
	void handleErrorEntry(String entry);
}