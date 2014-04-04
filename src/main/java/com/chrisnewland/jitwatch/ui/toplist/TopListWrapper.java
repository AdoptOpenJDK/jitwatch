package com.chrisnewland.jitwatch.ui.toplist;

import com.chrisnewland.jitwatch.toplist.ITopListVisitable;

import java.util.Arrays;

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
		return columns;
	}
}
