/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.test;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_CLOSE_PARENTHESES;
import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_EMPTY;
import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_OPEN_PARENTHESES;
import static org.junit.Assert.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.chrisnewland.jitwatch.model.bytecode.Instruction;
import com.chrisnewland.jitwatch.util.BytecodeUtil;

public class TestBytecodeUtil
{	
	@Test
	public void testParseBytecodes()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("0: ldc           #224                // int 1000000").append("\n");
		builder.append("2: istore_1").append("\n");
		builder.append("3: aload_0").append("\n");
		builder.append("4: arraylength").append("\n");
		builder.append("5: iconst_1").append("\n");
		builder.append("6: if_icmpne     28").append("\n");
		builder.append("9: aload_0").append("\n");
		builder.append("10: iconst_0").append("\n");
		builder.append("11: aaload").append("\n");

		List<Instruction> instructions = BytecodeUtil.parseInstructions(builder.toString());

		assertEquals(9, instructions.size());
		
		Instruction i0 = instructions.get(0);
		assertEquals(0, i0.getOffset());
		assertEquals("ldc", i0.getMnemonic());
		assertEquals(true, i0.hasParameters());
		assertEquals(1, i0.getParameters().length);
		assertEquals(224, i0.getParameters()[0]);
		assertEquals(true, i0.isParamConstant());
		assertEquals(true, i0.hasComment());
		assertEquals("// int 1000000", i0.getComment());
		
		Instruction i1 = instructions.get(1);
		assertEquals(2, i1.getOffset());
		assertEquals("istore_1", i1.getMnemonic());
		assertEquals(false, i1.hasParameters());
		assertEquals(0, i1.getParameters().length);
		assertEquals(false, i1.isParamConstant());
		assertEquals(false, i1.hasComment());
		assertEquals(null, i1.getComment());
		
		Instruction i5 = instructions.get(5);
		assertEquals(6, i5.getOffset());
		assertEquals("if_icmpne", i5.getMnemonic());
		assertEquals(1, i5.getParameters().length);
		assertEquals(true, i5.hasParameters());
		assertEquals(false, i5.isParamConstant());
		assertEquals(28, i5.getParameters()[0]);
		assertEquals(false, i5.hasComment());
		assertEquals(null, i5.getComment());
	}
	
	@Test
	public void testParseBytecodeRegressionIinc()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("0: lconst_0").append("\n");
		builder.append("1: lstore_2").append("\n");
		builder.append("2: iconst_0").append("\n");
		builder.append("3: lstore    2").append("\n");
		builder.append("4: iinc      4, 1").append("\n");
		builder.append("7: goto      47").append("\n");

		List<Instruction> instructions = BytecodeUtil.parseInstructions(builder.toString());

		assertEquals(6, instructions.size());
		
		Instruction i4 = instructions.get(4);
		assertEquals(4, i4.getOffset());
		assertEquals("iinc", i4.getMnemonic());
		assertEquals(true, i4.hasParameters());
		assertEquals(2, i4.getParameters().length);
		assertEquals(4, i4.getParameters()[0]);
		assertEquals(1, i4.getParameters()[1]);
		assertEquals(false, i4.isParamConstant());
		assertEquals(false, i4.hasComment());
	}
}