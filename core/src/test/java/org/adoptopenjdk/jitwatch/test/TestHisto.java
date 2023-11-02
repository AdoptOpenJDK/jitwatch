/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.*;

import org.adoptopenjdk.jitwatch.histo.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TestHisto
{
	/*
	 * Nearest rank percentile calculation from
	 * http://en.wikipedia.org/wiki/Percentile
	 */

	private Histo histo;
	@Before
	public void setUp() {
		histo = new Histo();
	}
	
	@Test
	public void testHistoPercentiles()
	{
		Histo h = new Histo();
		h.addValue(15);
		h.addValue(20);
		h.addValue(35);
		h.addValue(40);
		h.addValue(50);
		
		double epsilon = 1.0 / 1_000_000;
		
		assertEquals(20, h.getPercentile(30), epsilon);
		assertEquals(20, h.getPercentile(35), epsilon);
		assertEquals(35, h.getPercentile(40), epsilon);	
		
		assertEquals(0, h.getPercentile(0), epsilon);	
		assertEquals(50, h.getPercentile(100), epsilon);	

	}

	@Test
	public void testAddValue() {
		histo.addValue(5);
		histo.addValue(10);
		histo.addValue(5);
		histo.addValue(15);

		// to check the values are added correctly
		List<Map.Entry<Long, Integer>> sortedData = histo.getSortedData();
		assertEquals(4, sortedData.size());
	}

	@Test
	public void testClear() {
		histo.addValue(5);
		histo.addValue(10);
		histo.addValue(5);

		histo.clear();

		// check that the histogram is clear
		List<Map.Entry<Long, Integer>> sortedData = histo.getSortedData();
		assertEquals(0, sortedData.size());
		assertEquals(0, histo.getMaxCount());
		assertEquals(0, histo.getLastTime());
	}


	@Test
	public void testGetLastTime() {
		histo.addValue(5);
		histo.addValue(15);
		histo.addValue(10);
		// to get last but one item
		assertEquals(15, histo.getLastTime());
	}

	@Test
	public void testGetMaxCount() {
		histo.addValue(5);
		histo.addValue(5);
		histo.addValue(10);
		// to test max count of value
		assertEquals(2, histo.getMaxCount());
	}
}