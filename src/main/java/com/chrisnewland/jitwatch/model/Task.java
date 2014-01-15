/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import java.util.Map;

import com.chrisnewland.jitwatch.core.JITWatchConstants;
import com.chrisnewland.jitwatch.util.ParseUtil;

public class Task extends Tag
{
	private IParseDictionary parseDictionary;

	public Task(String name, Map<String, String> attrs, boolean selfClosing)
	{
		super(name, attrs, selfClosing);

		parseDictionary = new ParseDictionary();
	}

	public IParseDictionary getParseDictionary()
	{
		return parseDictionary;
	}

	public String decodeParseMethod(String method)
	{
		StringBuilder builder = new StringBuilder();
		
		Tag methodTag = parseDictionary.getMethod(method);
		
		String returnTypeID = methodTag.getAttrs().get(JITWatchConstants.ATTR_RETURN);

		String args = methodTag.getAttrs().get(JITWatchConstants.ATTR_ARGUMENTS);

		String methodName = methodTag.getAttrs().get(JITWatchConstants.ATTR_NAME);

		String klassId = methodTag.getAttrs().get(JITWatchConstants.ATTR_HOLDER);

		Tag klassTag = parseDictionary.getKlass(klassId);

		String klassName = klassTag.getAttrs().get(JITWatchConstants.ATTR_NAME);
		klassName = klassName.replace("/", ".");
		
		builder.append(" <!-- ");
		builder.append(getTypeOrKlass(returnTypeID));
		builder.append(" ");
		builder.append(klassName);
		builder.append(".");
		builder.append(methodName);
		builder.append("(");
		
		if (args != null && args.length() > 0)
		{
			String[] ids = args.split(" ");
			
			for(String id : ids)
			{
				builder.append(getTypeOrKlass(id));
				builder.append(",");
			}
			
			builder.deleteCharAt(builder.length()-1);
		}
		
		builder.append(") -->");

		return builder.toString();
	}
	
	private String getTypeOrKlass(String id)
	{
		Tag typeTag = parseDictionary.getType(id);
		
		String result = null;
		
		if (typeTag == null)
		{
			Tag klassTag = parseDictionary.getKlass(id);

			if (klassTag != null)
			{
				result = klassTag.getAttrs().get(JITWatchConstants.ATTR_NAME);
				result = result.replace("/", ".");
			}
		}
		else
		{
			result = typeTag.getAttrs().get(JITWatchConstants.ATTR_NAME);
		}

		if (result == null)
		{
			result = "???"; // further understanding required!
		}
		else
		{
			result = ParseUtil.expandParameterType(result);
		}
		
		return result;
	}
}
