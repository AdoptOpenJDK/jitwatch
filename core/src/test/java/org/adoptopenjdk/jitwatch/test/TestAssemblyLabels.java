/*
 * Copyright (c) 2013-2015 Philip Aston.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.Collections;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyLabels;
import org.junit.Test;

/**
 * Unit tests for {@link AssemblyLabels}.
 *
 * @author Philip Aston
 * @since 5.2
 */
public class TestAssemblyLabels
{
	private AssemblyLabels labels = new AssemblyLabels();
	private final StringBuilder sb = new StringBuilder();

	@Test
	public void testFormatEmpty()
	{
		final AssemblyInstruction localJump =
			new AssemblyInstruction(null, 65535, null, "jne", asList("0x000000000000ffff"), null, labels);
		
		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());
		sb.setLength(0);
		labels.formatOperands(localJump, sb);
		assertEquals(" 0x000000000000ffff", sb.toString());

		labels.buildLabels();

		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());
		sb.setLength(0);
		labels.formatOperands(localJump, sb);
		assertEquals(" 0x000000000000ffff", sb.toString());
	}
	
	@Test
	public void testFormatJumpLocal()
	{
		final AssemblyInstruction localJump =
			new AssemblyInstruction(null, 65535, null, "jne", asList("0x000000000000ffff"), null, labels);

		labels.newInstruction(localJump);

		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());
		sb.setLength(0);
		labels.formatOperands(localJump, sb);
		assertEquals(" 0x000000000000ffff", sb.toString());

		labels.buildLabels();
		
		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("             L0000", sb.toString());
		sb.setLength(0);
		labels.formatOperands(localJump, sb);
		assertEquals(" L0000", sb.toString());
	}

	@Test
	public void testFormatAddressJumpLocal2()
	{
		labels.newInstruction(new AssemblyInstruction("", 99, Collections.<String>emptyList(), "blah", Collections.<String>emptyList(), "", labels));
		labels.newInstruction(
			new AssemblyInstruction("anno", 65534, asList("mod"), "jne", asList("0x0000000000000100"), "", labels));
		labels.newInstruction(
			new AssemblyInstruction("anno", 65535, asList("mod"), "jne", asList("0x0000000000001000"), "", labels));

		sb.setLength(0);
		labels.formatAddress(256, sb);
		assertEquals("0x0000000000000100", sb.toString());
		sb.setLength(0);
		labels.formatAddress(4096, sb);
		assertEquals("0x0000000000001000", sb.toString());

		labels.buildLabels();

		sb.setLength(0);
		labels.formatAddress(256, sb);
		assertEquals("             L0000", sb.toString());
		sb.setLength(0);
		labels.formatAddress(4096, sb);
		assertEquals("             L0001", sb.toString());
		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());
	}

	@Test
	public void testFormatAddressJumpForeign()
	{
		labels.newInstruction(
			new AssemblyInstruction("anno", 65535, asList("mod"), "jne", asList("0x0000000000000000"), "", labels));

		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());

		labels.buildLabels();
		
		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());
	}
	
	@Test
	public void testFormatAddressNotJump()
	{
		labels.newInstruction(
			new AssemblyInstruction("anno", 65535, asList("mod"), "foo", asList("0x0000000000000000"), "", labels));

		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());

		labels.buildLabels();

		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());
	}
	
	@Test
	public void testFormatAddressNotJump2()
	{
		labels.newInstruction(
			new AssemblyInstruction("anno", 65535, asList("mod"), "jjj", asList("1", "2"), "", labels));

		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());

		labels.buildLabels();

		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());
	}

	@Test
	public void testFormatNoOperands()
	{
		final AssemblyInstruction noOps =
			new AssemblyInstruction(null, 65535, null, "jne", Collections.<String>emptyList(), null, labels);
		
		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());
		sb.setLength(0);
		labels.formatOperands(noOps, sb);
		assertTrue(sb.toString().isEmpty());

		labels.newInstruction(noOps);
		labels.buildLabels();

		sb.setLength(0);
		labels.formatAddress(65535, sb);
		assertEquals("0x000000000000ffff", sb.toString());
		sb.setLength(0);
		labels.formatOperands(noOps, sb);
		assertTrue(sb.toString().isEmpty());
	}
}
