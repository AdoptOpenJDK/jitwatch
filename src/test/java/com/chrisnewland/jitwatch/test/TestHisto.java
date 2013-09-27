/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.chrisnewland.jitwatch.histo.*;

public class TestHisto
{
	/*
	 * Nearest rank percentile calculation from
	 * http://en.wikipedia.org/wiki/Percentile
	 */
	
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
}
