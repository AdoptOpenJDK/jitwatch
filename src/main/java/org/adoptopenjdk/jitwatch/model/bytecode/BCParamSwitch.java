/*
 * Copyright (c) 2013-2015 Chris Newland.
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

import org.adoptopenjdk.jitwatch.util.StringUtil;

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

		for (String key : keyList)
		{
			String line = new StringBuilder(key).append(C_COLON).append(table.get(key)).toString();

			builder.append(StringUtil.alignRight(line, 16)).append(S_NEWLINE);
		}

		return builder.toString();
	}

	public String toString(int entryIndex)
	{
		StringBuilder builder = new StringBuilder();

		List<String> keyList = new ArrayList<>(table.keySet());

		Collections.sort(keyList);

		if (entryIndex < keyList.size())
		{
			String key = keyList.get(entryIndex);

			String line = new StringBuilder(key).append(C_COLON).append(table.get(key)).toString();

			builder.append(StringUtil.alignRight(line, 16));
		}

		return builder.toString();
	}

	public int getSize()
	{
		return table.size();
	}

	@Override
	public Map<String, String> getValue()
	{
		return table;
	}
}
