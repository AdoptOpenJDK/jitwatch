/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTable;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTableEntry;
import org.junit.Test;

public class TestLineTable
{
	@Test
	public void testCompositeLineTable()
	{
		LineTable table1 = new LineTable();
		LineTable table2 = new LineTable();

		List<String> paramList = new ArrayList<>();

		LineTableEntry entry1 = new LineTableEntry(MemberSignatureParts.fromParts("TestClass", "foo", void.class.getName(), paramList), 0, 0);
		LineTableEntry entry2 = new LineTableEntry(MemberSignatureParts.fromParts("TestClass", "foo", void.class.getName(), paramList), 5, 5);
		LineTableEntry entry3 = new LineTableEntry(MemberSignatureParts.fromParts("TestClass", "bar", void.class.getName(), paramList), 10, 10);
		LineTableEntry entry4 = new LineTableEntry(MemberSignatureParts.fromParts("TestClass", "bar", void.class.getName(), paramList), 15, 15);

		table1.add(entry1);
		table1.add(entry2);

		table2.add(entry3);
		table2.add(entry4);

		LineTable composite = new LineTable();
		composite.add(table2);
		composite.add(table1);
		composite.sort();

		assertEquals(0, composite.findSourceLineForBytecodeOffset(0));
		assertEquals(0, composite.findSourceLineForBytecodeOffset(1));
		assertEquals(5, composite.findSourceLineForBytecodeOffset(5));
		assertEquals(5, composite.findSourceLineForBytecodeOffset(6));
		assertEquals(10, composite.findSourceLineForBytecodeOffset(10));
		assertEquals(10, composite.findSourceLineForBytecodeOffset(11));
		assertEquals(15, composite.findSourceLineForBytecodeOffset(15));
		assertEquals(15, composite.findSourceLineForBytecodeOffset(16));

		assertEquals("foo", composite.getEntryForSourceLine(0).getMemberSignatureParts().getMemberName());
		assertEquals("foo", composite.getEntryForSourceLine(5).getMemberSignatureParts().getMemberName());
		assertEquals("bar", composite.getEntryForSourceLine(10).getMemberSignatureParts().getMemberName());
		assertEquals("bar", composite.getEntryForSourceLine(15).getMemberSignatureParts().getMemberName());
	}
}