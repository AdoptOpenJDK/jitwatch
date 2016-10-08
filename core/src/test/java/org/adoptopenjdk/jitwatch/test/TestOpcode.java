/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import java.util.HashSet;
import java.util.Set;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestOpcode
{
	@Test
	public void testIsAllocation()
	{
		Set<Opcode> allocations = new HashSet<>();
		
		for (Opcode opcode : Opcode.values())
		{
			if (opcode.isAllocation())
			{
				allocations.add(opcode);
			}
		}
		
		assertEquals(4, allocations.size());
		
		assertTrue(allocations.contains(Opcode.NEW));
		assertTrue(allocations.contains(Opcode.NEWARRAY));
		assertTrue(allocations.contains(Opcode.ANEWARRAY));
		assertTrue(allocations.contains(Opcode.MULTIANEWARRAY));
	}
}