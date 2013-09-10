package com.chrisnewland.jitwatch.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtil
{
	public static String formatTimestamp(long stamp, boolean showMillis)
	{
		long stampCopy = stamp;

		long hourMillis = 3600000L;
		long minuteMillis = 60000L;
		long secondMillis = 1000L;

		long hours = (long) Math.floor(stampCopy / hourMillis);
		stampCopy -= hours * hourMillis;

		long minutes = (long) Math.floor(stampCopy / minuteMillis);
		stampCopy -= minutes * minuteMillis;

		long seconds = (long) Math.floor(stampCopy / secondMillis);
		stampCopy -= seconds * secondMillis;

		long millis = stampCopy;

		StringBuilder sb = new StringBuilder();

		sb.append(pad(hours, 2)).append(":");
		sb.append(pad(minutes, 2)).append(":");
		sb.append(pad(seconds, 2));

		if (showMillis)
		{
			sb.append(".").append(pad(millis, 3));
		}

		return sb.toString();
	}

	public static String pad(long num, int width)
	{
		String numString = Long.toString(num);

		StringBuilder sb = new StringBuilder();

		int len = numString.length();

		if (len < width)
		{
			for (int i = 0; i < width - len; i++)
			{
				sb.append("0");
			}
		}

		sb.append(numString);

		return sb.toString();
	}

	public static List<String> textToList(String text, String split)
	{
		List<String> result = new ArrayList<>();

		if (text != null)
		{
			String[] lines = text.split(split);

			for (String line : lines)
			{
				if (line.trim().length() > 0)
				{
					result.add(line);
				}
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
	
	public static String makeUnqualified(String input)
	{
		int lastDot = input.lastIndexOf('.');
		
		String result = input;
		
		if (lastDot != -1)
		{
			result = input.substring(lastDot+1);
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
