/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.chrisnewland.jitwatch.util.StringUtil;

public class TestStringUtil
{
	@Test
	public void testFormatTimestamp()
	{
		assertEquals("00:00:00", StringUtil.formatTimestamp(0, false));
		assertEquals("00:00:00.000", StringUtil.formatTimestamp(0, true));
		assertEquals("00:01:00", StringUtil.formatTimestamp(60*1000+123, false));
		assertEquals("00:01:00.123", StringUtil.formatTimestamp(60*1000+123, true));
		assertEquals("01:00:00", StringUtil.formatTimestamp(60*60*1000+123, false));
		assertEquals("01:00:00.123", StringUtil.formatTimestamp(60*60*1000+123, true));		
	}
	
	@Test
	public void testGetLineAttributes()
	{
		Map<String, Map<String, String>> expected = new HashMap<>();

		String line1 = "foo='1' bar='2' baz='3'";

		Map<String, String> map1 = new HashMap<>();
		map1.put("foo", "1");
		map1.put("bar", "2");
		map1.put("baz", "3");

		String line2 = "";

		Map<String, String> map2 = new HashMap<>();

		expected.put(line1, map1);
		expected.put(line2, map2);

		for (Map.Entry<String, Map<String, String>> entry : expected.entrySet())
		{
			Map<String, String> result = StringUtil.getLineAttributes(entry.getKey());

			assertEquals(entry.getValue().size(), result.size());

			for (String key : entry.getValue().keySet())
			{
				assertTrue(result.containsKey(key));
				assertEquals(entry.getValue().get(key), result.get(key));
			}
		}
	}
}
