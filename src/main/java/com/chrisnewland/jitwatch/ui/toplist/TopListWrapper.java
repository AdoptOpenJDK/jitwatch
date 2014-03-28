package com.chrisnewland.jitwatch.ui.toplist;

import com.chrisnewland.jitwatch.toplist.ITopListVisitable;

public class TopListWrapper
{
	private String title;
	private ITopListVisitable visitable;
	private String[] columns;

	public TopListWrapper(String title, ITopListVisitable visitable, String[] columns)
	{
		this.title = title;
		this.visitable = visitable;
		this.columns = columns;
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
