/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.text.DecimalFormat;

public class StatsTableRow
{
	private final String name;
	private final long value;

	private static final DecimalFormat DF = new DecimalFormat("#,###");
	
	public StatsTableRow(String name, long value)
	{
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return DF.format(value);
	}
}