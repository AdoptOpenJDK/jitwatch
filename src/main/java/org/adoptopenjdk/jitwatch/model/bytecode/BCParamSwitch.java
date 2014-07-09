/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BCParamSwitch implements IBytecodeParam
{
	private Map<String, String> table = new HashMap<>();

	public BCParamSwitch()
	{

	}

	public void put(String key, String value)
	{
		table.put(key, value);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		List<String> keyList = new ArrayList<>(table.keySet());
		
		Collections.sort(keyList);
		
		builder.append(S_OPEN_BRACE).append(S_NEWLINE);
		
		for (String key : keyList)
		{
			builder.append(key).append(C_COLON).append(table.get(key)).append(S_NEWLINE);
		}
		
		builder.append(S_CLOSE_BRACE).append(S_NEWLINE);

		return builder.toString();
	}

	@Override
	public Map<String, String> getValue()
	{
		return table;
	}
}
