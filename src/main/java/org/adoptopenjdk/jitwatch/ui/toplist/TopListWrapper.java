/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.toplist;

import java.util.Arrays;

import org.adoptopenjdk.jitwatch.toplist.ITopListVisitable;

public class TopListWrapper
{
	private String title;
	private ITopListVisitable visitable;
	private String[] columns;

	public TopListWrapper(String title, ITopListVisitable visitable, String[] columns)
	{
		this.title = title;
		this.visitable = visitable;
        // Fixed after SonarQube critical warning: Security - Array is stored directly
		this.columns = Arrays.copyOf(columns, columns.length);
	}

	public String getTitle()
	{
		return title;
	}

	public ITopListVisitable getVisitable()
	{
		return visitable;
	}

	public String[] getColumns()
	{
		return Arrays.copyOf(columns, columns.length);
	}
}
