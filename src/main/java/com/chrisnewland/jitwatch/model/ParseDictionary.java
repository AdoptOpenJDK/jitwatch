package com.chrisnewland.jitwatch.model;

import java.util.HashMap;
import java.util.Map;

public class ParseDictionary implements IParseDictionary
{
	private Map<String, Tag> typeMap = new HashMap<>();
	private Map<String, Tag> klassMap = new HashMap<>();
	private Map<String, Tag> methodMap = new HashMap<>();

	@Override
	public Tag getType(String id)
	{
		return typeMap.get(id);
	}

	@Override
	public Tag getKlass(String id)
	{
		return klassMap.get(id);
	}

	@Override
	public Tag getMethod(String id)
	{
		return methodMap.get(id);
	}

	@Override
	public void setType(String id, Tag type)
	{
		typeMap.put(id, type);
	}

	@Override
	public void setKlass(String id, Tag klass)
	{
		klassMap.put(id, klass);
	}

	@Override
	public void setMethod(String id, Tag method)
	{
		methodMap.put(id, method);
	}
}
