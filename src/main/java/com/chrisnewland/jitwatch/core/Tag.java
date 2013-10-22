package com.chrisnewland.jitwatch.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tag
{
	private String name;
	private Map<String, String> attrs = new HashMap<>();
	private List<Tag> children = new ArrayList<>();
	private Tag parent = null;

	public Tag(String name, Map<String, String> attrs)
	{
		this.name = name;
		this.attrs = attrs;
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
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append('<').append(name);
		
		if (attrs.size() > 0)
		{
			for (Map.Entry<String, String> entry : attrs.entrySet())
			{
				builder.append(' ').append(entry.getKey()).append("=\"");
				builder.append(entry.getValue()).append('"');
			}
		}

		if (children.size() > 0)
		{
			builder.append(">\n");
			
			for (Tag child : children)
			{
				builder.append(child.toString());
			}
			
			builder.append("</").append(name).append(">\n");
		}
		else
		{
			builder.append("/>\n");
		}		
		
		return builder.toString();
	}
}