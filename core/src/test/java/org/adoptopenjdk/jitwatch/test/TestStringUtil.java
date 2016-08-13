/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.junit.Test;

public class TestStringUtil
{
	@Test
	public void testFormatTimestamp()
	{
		assertEquals("00:00:00", StringUtil.formatTimestamp(0, false));
		assertEquals("00:00:00.000", StringUtil.formatTimestamp(0, true));
		assertEquals("00:00:04", StringUtil.formatTimestamp(4000, false));
		assertEquals("00:00:04.567", StringUtil.formatTimestamp(4567, true));
		assertEquals("00:01:00", StringUtil.formatTimestamp(60 * 1000 + 123, false));
		assertEquals("00:01:00.123", StringUtil.formatTimestamp(60 * 1000 + 123, true));
		assertEquals("01:00:00", StringUtil.formatTimestamp(60 * 60 * 1000 + 123, false));
		assertEquals("01:00:00.123", StringUtil.formatTimestamp(60 * 60 * 1000 + 123, true));
		assertEquals("1d 01:00:00.123", StringUtil.formatTimestamp(25*60 * 60 * 1000 + 123, true));

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

		String line2 = S_EMPTY;

		Map<String, String> map2 = new HashMap<>();

		expected.put(line1, map1);
		expected.put(line2, map2);

		for (Map.Entry<String, Map<String, String>> entry : expected.entrySet())
		{
			Map<String, String> result = StringUtil.attributeStringToMap(entry.getKey());

			assertEquals(entry.getValue().size(), result.size());

			for (String key : entry.getValue().keySet())
			{
				assertTrue(result.containsKey(key));
				assertEquals(entry.getValue().get(key), result.get(key));
			}
		}
	}

	@Test
	public void testGetAttributesRegression()
	{
		String line = "<task compile_id='21' method='java/util/Properties loadConvert ([CII[C)Ljava/lang/String;' bytes='505' count='10000' backedge_count='5668' iicount='108' stamp='6.801'>";

		Map<String, String> result = StringUtil.attributeStringToMap(line);

		assertEquals("21", result.get("compile_id"));
		assertEquals("java/util/Properties loadConvert ([CII[C)Ljava/lang/String;", result.get("method"));
		assertEquals("505", result.get("bytes"));
		assertEquals("10000", result.get("count"));
		assertEquals("5668", result.get("backedge_count"));
		assertEquals("108", result.get("iicount"));
		assertEquals("6.801", result.get("stamp"));
	}
}
