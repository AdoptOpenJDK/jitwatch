/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.stats;

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