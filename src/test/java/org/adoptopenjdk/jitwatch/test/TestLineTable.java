/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.adoptopenjdk.jitwatch.model.bytecode.LineTable;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTableEntry;
import org.junit.Test;

public class TestLineTable
{
	@Test
	public void testCompositeLineTable()
	{
		LineTable table1 = new LineTable(null);
		LineTable table2 = new LineTable(null);

		LineTableEntry entry1 = new LineTableEntry(0, 0);
		LineTableEntry entry2 = new LineTableEntry(5, 5);
		LineTableEntry entry3 = new LineTableEntry(10, 10);
		LineTableEntry entry4 = new LineTableEntry(15, 15);

		table1.add(entry1);
		table1.add(entry2);

		table2.add(entry3);
		table2.add(entry4);

		LineTable composite = new LineTable(null);
		composite.add(table2);
		composite.add(table1);

		assertEquals(0, composite.findSourceLineForBytecodeOffset(0));
		assertEquals(0, composite.findSourceLineForBytecodeOffset(1));
		assertEquals(5, composite.findSourceLineForBytecodeOffset(5));
		assertEquals(5, composite.findSourceLineForBytecodeOffset(6));
		assertEquals(10, composite.findSourceLineForBytecodeOffset(10));
		assertEquals(10, composite.findSourceLineForBytecodeOffset(11));
		assertEquals(15, composite.findSourceLineForBytecodeOffset(15));
		assertEquals(15, composite.findSourceLineForBytecodeOffset(16));

		assertTrue(table1.sourceLineInRange(0));
		assertTrue(table1.sourceLineInRange(1));
		assertTrue(table1.sourceLineInRange(5));

		assertFalse(table1.sourceLineInRange(-1));
		assertFalse(table1.sourceLineInRange(6));
		assertFalse(table1.sourceLineInRange(10));

		assertTrue(table2.sourceLineInRange(10));
		assertTrue(table2.sourceLineInRange(11));
		assertTrue(table2.sourceLineInRange(15));

		assertFalse(table2.sourceLineInRange(-1));
		assertFalse(table2.sourceLineInRange(6));
		assertFalse(table2.sourceLineInRange(25));

		assertTrue(composite.sourceLineInRange(1));
		assertTrue(composite.sourceLineInRange(11));
		assertTrue(composite.sourceLineInRange(15));

		assertFalse(composite.sourceLineInRange(-1));
		assertFalse(composite.sourceLineInRange(25));
	}
}