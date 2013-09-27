/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

public class AttributeTableRow3Col
{
	private final String type;
	private final String name;
	private final String value;

	public AttributeTableRow3Col(String type, String name, String value)
	{
		this.type = type;
		this.name = name;
		this.value = value;
	}

	public String getType()
	{
		return type;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}
}
