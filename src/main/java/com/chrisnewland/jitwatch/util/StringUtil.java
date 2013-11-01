/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtil
{
	private static final char QUOTE = '\'';
	private static final char SPACE = ' ';
	private static final char EQUALS = '=';
	
    private static final DecimalFormat DF = new DecimalFormat("#,###");

	public static String formatTimestamp(long stamp, boolean showMillis)
	{
	    if (showMillis && stamp <= 1000)
	    {
	        return "0." + stamp;
	    }
	    
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
			result = input.substring(lastDot + 1);
		}

		return result;
	}

	public static Map<String, String> getLineAttributes(String line)
	{
		Map<String, String> result = new HashMap<>();

		if (line != null)
		{
			int len = line.length();

			StringBuilder key = new StringBuilder();
			StringBuilder val = new StringBuilder();

			boolean inValue = false;

			for (int i = 0; i < len; i++)
			{
				char c = line.charAt(i);
				
				switch (c)
				{
				case SPACE:
					if (!inValue)
					{
						//space before new key
						key.delete(0, key.length());
					}
					else
					{
						val.append(SPACE);
					}
					break;
				case QUOTE:
					if (inValue)
					{
						//finished attr
						result.put(key.toString(), val.toString());
						key.delete(0, key.length());
						val.delete(0, val.length());
						inValue = false;
					}
					else
					{
						inValue = true;
					}
					break;
				case EQUALS:
					if (inValue)
					{
						val.append(EQUALS);
					}
					break;
				default:
					if (inValue)
					{
						val.append(c);
					}
					else
					{
						key.append(c);
					}
				}
			}
		}

		return result;
	}
	
	public static String formatThousands(String value)
	{
        // see if it can be formatted as a long with commas at thousands
        try
        {
            value = DF.format(Long.parseLong(value));
        }
        catch (NumberFormatException nfe)
        {
        }
        
        return value;
	}

}
