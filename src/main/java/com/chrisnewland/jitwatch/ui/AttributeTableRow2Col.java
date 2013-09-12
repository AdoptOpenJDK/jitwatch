package com.chrisnewland.jitwatch.ui;

public class AttributeTableRow2Col
{
	private final String name;
	private final long value;

	public AttributeTableRow2Col(String name, long value)
	{
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public long getValue()
	{
		return value;
	}
}