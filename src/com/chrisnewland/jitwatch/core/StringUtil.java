package com.chrisnewland.jitwatch.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtil
{
	public static List<String> textToList(String text, String split)
	{
		List<String> result = new ArrayList<>();

		if (text != null)
		{
			String[] lines = text.split(split);

			if (lines.length > 0)
			{
				result.addAll(Arrays.asList(lines));
			}
		}

		return result;
	}

	public static String listToText(List<String> list, String join)
	{
		StringBuilder builder = new StringBuilder();

		for (String line : list)
		{
			builder.append(line).append(join);
		}

		if (builder.length() > 0)
		{
			builder.deleteCharAt(builder.length() - 1);
		}

		return builder.toString();
	}

	public static String getSubstringBetween(String input, String start, String end)
	{
		int startPos = input.indexOf(start);

		String result = null;

		if (startPos != -1)
		{
			int endPos = input.indexOf(end, startPos + start.length());

			if (endPos != -1)
			{
				result = input.substring(startPos + start.length(), endPos);
			}

		}

		return result;
	}

	public static Map<String, String> getLineAttributes(String line)
	{
		String[] spaceSep = line.split(" ");

		Map<String, String> result = new HashMap<>();

		for (String part : spaceSep)
		{
			String[] kvParts = part.split("=");

			if (kvParts.length == 2)
			{
				String key = kvParts[0];
				String value = StringUtil.getSubstringBetween(kvParts[1], "'", "'");

				result.put(key, value);
			}
		}

		return result;
	}
}
