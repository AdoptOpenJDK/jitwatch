/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_CLOSE_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOUBLE_QUOTE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_EQUALS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.util.StringUtil;

public class Tag
{
	private String name;
	private String attributeString;
	private List<Tag> children = new ArrayList<>();
	private Tag parent = null;
	private boolean selfClosing = false;
	private boolean isFragment = false;
	private String textContent = null;
	
	private static final String INDENT = "  ";

	public Tag(String name, String attributeString, boolean selfClosing)
	{
		this.name = name;
		this.attributeString = attributeString;
		this.selfClosing = selfClosing;
	}

	public void addTextContent(String text)
	{
		if (textContent == null)
		{
			textContent = text;
		}
		else
		{
			textContent += text;
		}
	}

	public String getTextContent()
	{
		return textContent;
	}

	public void addChild(Tag child)
	{
		child.setParent(this);
		children.add(child);
	}

	public List<Tag> getChildren()
	{
		return Collections.unmodifiableList(children);
	}

	public boolean isSelfClosing()
	{
		return selfClosing;
	}
	
	public String getClosingTag()
	{		
		StringBuilder builder = new StringBuilder();
		builder.append(C_OPEN_ANGLE).append(C_SLASH).append(name).append(C_CLOSE_ANGLE);
		
		return builder.toString();
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
				Map<String, String> attributes = child.getAttributes();
				
				if (attrValue != null && attrValue.equals(attributes.get(attrName)))
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

	public Map<String, String> getAttributes()
	{
		return StringUtil.attributeStringToMap(attributeString);
	}


	@Override
	public String toString()
	{
		return toString(true);
	}

	public String toString(boolean showChildren) {
		return XMLStringBuilder.buildXMLString(this, showChildren);
	}


	@Override
    public boolean equals(Object o) {
        if (this == o)
		{
			return true;
		}
        if (o == null || getClass() != o.getClass())
		{
			return false;
		}

        Tag tag = (Tag) o;

        if (selfClosing != tag.selfClosing)
		{
			return false;
		}
        if (attributeString != null ? !attributeString.equals(tag.attributeString) : tag.attributeString != null)
		{
			return false;
		}
        if (children != null ? !children.equals(tag.children) : tag.children != null)
		{
			return false;
		}
        if (name != null ? !name.equals(tag.name) : tag.name != null)
		{
			return false;
		}
        if (parent != null ? !parent.equals(tag.parent) : tag.parent != null)
		{
			return false;
		}
        if (textContent != null ? !textContent.equals(tag.textContent) : tag.textContent != null)
		{
			return false;
		}

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (attributeString != null ? attributeString.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (selfClosing ? 1 : 0);
        result = 31 * result + (textContent != null ? textContent.hashCode() : 0);
        return result;
    }

	public boolean isFragment()
	{
		return isFragment;
	}

	public void setFragment(boolean isFragment)
	{
		this.isFragment = isFragment;
	}
	int getDepth() {
		if (parent != null) {
			return 1 + parent.getDepth();
		} else {
			return 0;
		}
	}
}
