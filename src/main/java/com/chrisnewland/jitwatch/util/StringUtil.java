/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public final class StringUtil
{
	private static final DecimalFormat DF = new DecimalFormat("#,###");

    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private StringUtil() {
    }

	public static String formatTimestamp(long stamp, boolean showMillis)
	{
		if (stamp == 0)
		{
			if (showMillis)
			{
				return "0.000";
			}
			else
			{
				return "0";
			}
		}

		long stampCopy = stamp;

		long dayMillis = 24L * 60L * 60_000L;
		long hourMillis = 60L * 60_000L;
		long minuteMillis = 60_000L;
		long secondMillis = 1_000L;

		long days = (long) Math.floor(stampCopy / dayMillis);
		stampCopy -= days * dayMillis;

		long hours = (long) Math.floor(stampCopy / hourMillis);
		stampCopy -= hours * hourMillis;

		long minutes = (long) Math.floor(stampCopy / minuteMillis);
		stampCopy -= minutes * minuteMillis;

		long seconds = (long) Math.floor(stampCopy / secondMillis);
		stampCopy -= seconds * secondMillis;

		long millis = stampCopy;

		StringBuilder sb = new StringBuilder();

		if (days > 0)
		{
			sb.append(days).append("d ");
		}

		sb.append(padZero(hours, 2)).append(S_COLON);
		sb.append(padZero(minutes, 2)).append(S_COLON);
		sb.append(padZero(seconds, 2));

		if (showMillis)
		{
			sb.append(S_DOT).append(padZero(millis, 3));
		}

		return sb.toString();
	}
	
	public static String repeat(char c, int count)
	{
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < count; i++)
		{
			builder.append(c);
		}
		
		return builder.toString();		
	}
	
	public static String rtrim(String string)
	{
		return string.replaceAll("\\s+$","");
	}
	
	public static String padLeft(long num, int width)
	{
		return pad(Long.toString(num), width, C_SPACE, true);
	}
	
	public static String padLeft(String str, int width)
	{
		return pad(str, width, C_SPACE, true);
	}	
	
	public static String padRight(long num, int width)
	{
		return pad(Long.toString(num), width, C_SPACE, false);
	}
	
	public static String padRight(String str, int width)
	{
		return pad(str, width, C_SPACE, false);
	}	
	
	public static String padZero(long num, int width)
	{
		return pad(Long.toString(num), width, '0', true);
	}	
	
	public static String pad(String str, int width, char padding, boolean left)
	{
		StringBuilder sb = new StringBuilder();

		int len = str.length();
		
		if (!left)
		{
			sb.append(str);
		}

		if (len < width)
		{
			for (int i = 0; i < width - len; i++)
			{
				sb.append(padding);
			}
		}

		if (left)
		{
			sb.append(str);
		}
		
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

	public static String makeUnqualified(String fqClassName)
	{
		int lastDot = fqClassName.lastIndexOf('.');

		String result = fqClassName;

		if (lastDot != -1)
		{
			result = fqClassName.substring(lastDot + 1);
		}

		return result;
	}
	
	public static String getPackageName(String fqClassName)
	{
		int lastDot = fqClassName.lastIndexOf('.');

		String result = fqClassName;

		if (lastDot != -1)
		{
			result = fqClassName.substring(0, lastDot);
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
				case C_SPACE:
					if (!inValue)
					{
						// C_SPACE before new key
						key.delete(0, key.length());
					}
					else
					{
						val.append(C_SPACE);
					}
					break;
				case C_QUOTE:
					if (inValue)
					{
						// finished attr
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
				case C_EQUALS:
					if (inValue)
					{
						val.append(C_EQUALS);
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
	
	public static Map<String, String> getLineAttributesDoubleQuote(String line)
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
				case C_SPACE:
					if (!inValue)
					{
						// C_SPACE before new key
						key.delete(0, key.length());
					}
					else
					{
						val.append(C_SPACE);
					}
					break;
				case C_DOUBLE_QUOTE:
					if (inValue)
					{
						// finished attr
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
				case C_EQUALS:
					if (inValue)
					{
						val.append(C_EQUALS);
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

	public static String formatThousands(String inValue)
	{
        String value = inValue;
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