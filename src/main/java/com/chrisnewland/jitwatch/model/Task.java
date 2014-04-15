/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import com.chrisnewland.jitwatch.core.JITWatchConstants;
import com.chrisnewland.jitwatch.util.ParseUtil;

import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class Task extends Tag
{
	private IParseDictionary parseDictionary;
	private CompilerName compiler;

	public Task(String name, Map<String, String> attrs, boolean selfClosing, CompilerName compiler)
	{
		super(name, attrs, selfClosing);
		
		this.compiler = compiler;
		parseDictionary = new ParseDictionary();
	}
	
	public CompilerName getCompiler()
	{
		return compiler;
	}

	public IParseDictionary getParseDictionary()
	{
		return parseDictionary;
	}
	
	public void addDictionaryType(String type, Tag tag)
	{
		parseDictionary.setType(type, tag);
	}
	
	public void addDictionaryMethod(String method, Tag tag)
	{
		parseDictionary.setMethod(method, tag);
	}
	
	public void addDictionaryKlass(String klass, Tag tag)
	{
		parseDictionary.setKlass(klass, tag);
	}

	public String decodeParseMethod(String method)
	{
		StringBuilder builder = new StringBuilder();
		
		Tag methodTag = parseDictionary.getMethod(method);
		
		String returnTypeID = methodTag.getAttribute(JITWatchConstants.ATTR_RETURN);

		String args = methodTag.getAttribute(JITWatchConstants.ATTR_ARGUMENTS);

		String methodName = methodTag.getAttribute(JITWatchConstants.ATTR_NAME);

		String klassId = methodTag.getAttribute(JITWatchConstants.ATTR_HOLDER);

		Tag klassTag = parseDictionary.getKlass(klassId);

		String klassName = klassTag.getAttribute(JITWatchConstants.ATTR_NAME);
		klassName = klassName.replace(S_SLASH, S_DOT);
		
		builder.append(" <!-- ");
		builder.append(getTypeOrKlass(returnTypeID));
		builder.append(C_SPACE);
		builder.append(klassName);
		builder.append(S_DOT);
		builder.append(methodName);
		builder.append(S_OPEN_PARENTHESES);
		
		if (args != null && args.length() > 0)
		{
			String[] ids = args.split(S_SPACE);
			
			for(String id : ids)
			{
				builder.append(getTypeOrKlass(id));
				builder.append(S_COMMA);
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
				result = klassTag.getAttribute(JITWatchConstants.ATTR_NAME);
				result = result.replace(S_SLASH, S_DOT);
			}
		}
		else
		{
			result = typeTag.getAttribute(JITWatchConstants.ATTR_NAME);
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
