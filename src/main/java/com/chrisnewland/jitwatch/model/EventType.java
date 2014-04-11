package com.chrisnewland.jitwatch.model;

public enum EventType
{
	QUEUE("Queued"), NMETHOD_C1("Compiled (C1)"), NMETHOD_C2("Compiled (C2)"), NMETHOD_C2N("Compiled (C2N)"), TASK("Compile Detail");

	EventType(String text)
	{
		this.text = text;
	}

	private final String text;

	public String getText()
	{
		return text;
	}
}
