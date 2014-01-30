/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class Tag
{
	private String name;
	private Map<String, String> attrs = new HashMap<>();
	private List<Tag> children = new ArrayList<>();
	private Tag parent = null;
	private boolean selfClosing = false;

	private static final String INDENT = "  ";

	public Tag(String name, Map<String, String> attrs, boolean selfClosing)
	{
		this.name = name;
		this.attrs = attrs;
		this.selfClosing = selfClosing;
	}

	public void addChild(Tag child)
	{
		child.setParent(this);
		children.add(child);
	}

	public List<Tag> getChildren()
	{
		return children;
	}

	public Tag getFirstNamedChild(String name)
	{
		List<Tag> namedChildren = getNamedChildren(name);

		if (namedChildren.size() > 0)
		{
			return namedChildren.get(0);
		}
		else
		{
			return null;
		}
	}

	public List<Tag> getNamedChildren(String name)
	{
		List<Tag> result = new ArrayList<>();

		for (Tag child : children)
		{
			if (child.getName().equals(name))
			{
				result.add(child);
			}
		}

		return result;
	}

	public List<Tag> getNamedChildrenWithAttribute(String tagName, String attrName, String attrValue)
	{
		List<Tag> result = new ArrayList<>();

		for (Tag child : children)
		{
			if (child.getName().equals(tagName))
			{
				if (child.getAttrs().containsKey(attrName) && child.getAttrs().get(attrName).equals(attrValue))
				{
					result.add(child);
				}
			}
		}

		return result;
	}

	public Tag getParent()
	{
		return parent;
	}

	public void setParent(Tag parent)
	{
		this.parent = parent;
	}

	public String getName()
	{
		return name;
	}

	public Map<String, String> getAttrs()
	{
		return attrs;
	}

	private int getDepth(Tag tag)
	{
		if (tag.getParent() != null)
		{
			return 1 + getDepth(tag.getParent());
		}
		else
		{
			return 0;
		}
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		int myDepth = getDepth(this);

		for (int i = 0; i < myDepth; i++)
		{
			builder.append(INDENT);
		}

		builder.append('<').append(name);

		if (attrs.size() > 0)
		{
			for (Map.Entry<String, String> entry : attrs.entrySet())
			{
				builder.append(C_SPACE).append(entry.getKey()).append("=\"");
				builder.append(entry.getValue()).append(C_DOUBLE_QUOTE);
			}
		}

		if (selfClosing)
		{
			builder.append("/>\n");
		}
		else
		{
			if (children.size() > 0)
			{
				builder.append(">\n");

				for (Tag child : children)
				{
					builder.append(child.toString());
				}
			}
			else
			{
				builder.append(">\n");
			}

			for (int i = 0; i < myDepth; i++)
			{
				builder.append(INDENT);
			}

			builder.append("</").append(name).append(">\n");
		}

		return builder.toString();
	}
}