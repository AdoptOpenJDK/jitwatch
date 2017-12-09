/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import java.util.HashMap;
import java.util.Map;

public class TimerUtil
{
	private static Map<String, Long> timestampMap = new HashMap<>();

	private static long lastNow = 0;

	public static void timerStart(String key)
	{
		long now = System.currentTimeMillis();

		timestampMap.put(key, now);

		long delta = now - lastNow;

		if (lastNow != 0)
		{
			System.out.println(now + " (+" + delta + ") starting " + key);
		}
		else
		{
			System.out.println(now + " starting " + key);
		}

		lastNow = now;
	}

	public static void timerEnd(String key)
	{
		if (timestampMap.containsKey(key))
		{
			long start = timestampMap.get(key);

			long now = System.currentTimeMillis();

			System.out.println(key + ": " + (now - start) + "ms");
		}
		else
		{
			System.out.println("No start time found for " + key);
		}
	}
}