/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

public class ParseDictionary implements IParseDictionary
{
	private Map<String, Tag> typeMap = new HashMap<>();
	private Map<String, Tag> klassMap = new HashMap<>();
	private Map<String, Tag> methodMap = new HashMap<>();

	private String parseMethod;

	private Map<String, BCIOpcodeMap> methodBCIOpcodeMap = new HashMap<>();

	public ParseDictionary(String parseMethod)
	{
		this.parseMethod = parseMethod;
	}

	@Override
	public String getParseMethod()
	{
		return parseMethod;
	}

	@Override
	public void putBCIOpcode(String methodID, int bci, Opcode opcode)
	{
		BCIOpcodeMap bciOpcodeMap = getBCIOpcodeMap(methodID);

		if (bciOpcodeMap == null)
		{
			bciOpcodeMap = new BCIOpcodeMap();
			methodBCIOpcodeMap.put(methodID, bciOpcodeMap);
		}

		bciOpcodeMap.put(bci, opcode);
	}

	@Override
	public BCIOpcodeMap getBCIOpcodeMap(String methodID)
	{
		return methodBCIOpcodeMap.get(methodID);
	}

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
	public void putType(String id, Tag type)
	{
		typeMap.put(id, type);
	}

	@Override
	public void putKlass(String id, Tag klass)
	{
		klassMap.put(id, klass);
	}

	@Override
	public void putMethod(String id, Tag method)
	{
		methodMap.put(id, method);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Types:\n");

		for (Map.Entry<String, Tag> entry : typeMap.entrySet())
		{
			builder.append(entry.getKey()).append("\t=>\t").append(entry.getValue().toString().trim()).append(S_NEWLINE);
		}

		builder.append("Klasses:\n");

		for (Map.Entry<String, Tag> entry : klassMap.entrySet())
		{
			builder.append(entry.getKey()).append("\t=>\t").append(entry.getValue().toString().trim()).append(S_NEWLINE);
		}

		builder.append("Methods:\n");

		for (Map.Entry<String, Tag> entry : methodMap.entrySet())
		{
			builder.append(entry.getKey()).append("\t=>\t").append(entry.getValue().toString().trim()).append(S_NEWLINE);
		}

		return builder.toString().trim();
	}
}