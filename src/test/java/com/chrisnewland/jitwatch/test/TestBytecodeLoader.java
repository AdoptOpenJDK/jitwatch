/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.test;

import com.chrisnewland.jitwatch.loader.BytecodeLoader;
import com.chrisnewland.jitwatch.model.MetaMethod;
import com.chrisnewland.jitwatch.model.bytecode.*;
import com.chrisnewland.jitwatch.util.ClassUtil;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestBytecodeLoader
{
	private Method getMethod(String fqClassName, String method, Class<?>[] paramTypes)
	{
		Method m = null;

		try
		{
			Class<?> clazz = ClassUtil.loadClassWithoutInitialising(fqClassName);
			m = clazz.getDeclaredMethod(method, paramTypes);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return m;
	}

	@Test
	public void testBytecodeSignature()
	{
	    String className = "java.lang.StringBuilder";
	    String methodName = "charAt";
	    
		Method m = getMethod(className, methodName, new Class<?>[] { int.class });
		MetaMethod method = new MetaMethod(m, null);

		String bcSig = method.getSignatureForBytecode();
		
		ClassBC classBytecode = BytecodeLoader.fetchBytecodeForClass(new ArrayList<String>(), className);
		
		List<Instruction> instructions = classBytecode.getMemberBytecode(bcSig);
				
		assertNotNull(instructions);		
	}
	
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

		List<Instruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(9, instructions.size());
		
		Instruction i0 = instructions.get(0);
		assertEquals(0, i0.getOffset());
		assertEquals(Opcode.LDC, i0.getOpcode());
		assertEquals(true, i0.hasParameters());
		assertEquals(1, i0.getParameters().size());
		
		IBytecodeParam paramI0 = i0.getParameters().get(0);
		assertTrue(paramI0 instanceof BCParamConstant);		
		assertEquals(224, paramI0.getValue());

		assertEquals(true, i0.hasComment());
		assertEquals("// int 1000000", i0.getComment());
		
		Instruction i1 = instructions.get(1);
		assertEquals(2, i1.getOffset());
		assertEquals(Opcode.ISTORE_1, i1.getOpcode());
		assertEquals(false, i1.hasParameters());
		assertEquals(0, i1.getParameters().size());
		assertEquals(false, i1.hasComment());
		assertEquals(null, i1.getComment());
		
		Instruction i5 = instructions.get(5);
		assertEquals(6, i5.getOffset());
		assertEquals(Opcode.IF_ICMPNE, i5.getOpcode());
		assertEquals(1, i5.getParameters().size());
		assertEquals(true, i5.hasParameters());
		
		IBytecodeParam paramI5 = i5.getParameters().get(0);
		assertTrue(paramI5 instanceof BCParamNumeric);		
		assertEquals(28, paramI5.getValue());
		
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

		List<Instruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(6, instructions.size());
		
		Instruction i4 = instructions.get(4);
		assertEquals(4, i4.getOffset());
		assertEquals(Opcode.IINC, i4.getOpcode());
		assertEquals(true, i4.hasParameters());
		assertEquals(2, i4.getParameters().size());
		
		IBytecodeParam paramI4a = i4.getParameters().get(0);
		IBytecodeParam paramI4b = i4.getParameters().get(1);

		assertTrue(paramI4a instanceof BCParamNumeric);
		assertTrue(paramI4b instanceof BCParamNumeric);		

		assertEquals(4, paramI4a.getValue());
		assertEquals(1, paramI4b.getValue());

		assertEquals(false, i4.hasComment());
	}
	
	@Test
	public void testParseBytecodeRegressionNegativeInc()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("0: lconst_0").append("\n");
		builder.append("1: lstore_2").append("\n");
		builder.append("2: iconst_0").append("\n");
		builder.append("3: lstore    2").append("\n");
		builder.append("4: iinc      4, -1").append("\n");
		builder.append("7: goto      47").append("\n");

		List<Instruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(6, instructions.size());
		
		Instruction i4 = instructions.get(4);
		assertEquals(4, i4.getOffset());
		assertEquals(Opcode.IINC, i4.getOpcode());
		assertEquals(true, i4.hasParameters());
		assertEquals(2, i4.getParameters().size());
		
		IBytecodeParam paramI4a = i4.getParameters().get(0);
		IBytecodeParam paramI4b = i4.getParameters().get(1);

		assertTrue(paramI4a instanceof BCParamNumeric);
		assertTrue(paramI4b instanceof BCParamNumeric);		

		assertEquals(4, paramI4a.getValue());
		assertEquals(-1, paramI4b.getValue());

		assertEquals(false, i4.hasComment());
	}
	
	@Test
	public void testParseBytecodeRegressionNewArray()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("3: newarray       int").append("\n");
	
		List<Instruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(1, instructions.size());
		
		Instruction i0 = instructions.get(0);
		assertEquals(3, i0.getOffset());
		assertEquals(Opcode.NEWARRAY, i0.getOpcode());
		assertEquals(true, i0.hasParameters());
		assertEquals(1, i0.getParameters().size());

		IBytecodeParam paramI0 = i0.getParameters().get(0);
		assertTrue(paramI0 instanceof BCParamString);		
		assertEquals("int", paramI0.getValue());
		
		assertEquals(false, i0.hasComment());
	}
	
	@Test
	public void testParseBytecodeRegressionTableSwitch()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("0: lconst_0").append("\n");
		builder.append("1: lstore_2").append("\n");
		builder.append("2: iconst_0").append("\n");
		builder.append("3: tableswitch   { // -1 to 5").append("\n");
		builder.append("             -1: 100").append("\n");
		builder.append("              0: 101").append("\n");
		builder.append("              1: 102").append("\n");
		builder.append("              2: 103").append("\n");
		builder.append("              3: 104").append("\n");
		builder.append("              4: 105").append("\n");
		builder.append("              5: 106").append("\n");
		builder.append("        default: 107").append("\n");
		builder.append("}").append("\n");
		builder.append("99: lstore_2").append("\n");
		
		List<Instruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(5, instructions.size());
		
		Instruction i3 = instructions.get(3);
		assertEquals(3, i3.getOffset());
		assertEquals(Opcode.TABLESWITCH, i3.getOpcode());
		assertEquals(true, i3.hasParameters());
		assertEquals(1, i3.getParameters().size());

		IBytecodeParam paramI0 = i3.getParameters().get(0);
		assertTrue(paramI0 instanceof BCParamSwitch);		
	}
	
	@Test
	public void testParseBytecodeRegressionLookupSwitch()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("0: lconst_0").append("\n");
		builder.append("1: lstore_2").append("\n");
		builder.append("2: iconst_0").append("\n");
		builder.append("3: lookupswitch   { // -1 to 5").append("\n");
		builder.append("             -1: 100").append("\n");
		builder.append("              0: 101").append("\n");
		builder.append("              1: 102").append("\n");
		builder.append("              2: 103").append("\n");
		builder.append("              3: 104").append("\n");
		builder.append("              4: 105").append("\n");
		builder.append("              5: 106").append("\n");
		builder.append("        default: 107").append("\n");
		builder.append("}").append("\n");
		builder.append("99: lstore_2").append("\n");
		
		List<Instruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(5, instructions.size());
		
		Instruction i3 = instructions.get(3);
		assertEquals(3, i3.getOffset());
		assertEquals(Opcode.LOOKUPSWITCH, i3.getOpcode());
		assertEquals(true, i3.hasParameters());
		assertEquals(1, i3.getParameters().size());

		IBytecodeParam paramI0 = i3.getParameters().get(0);
		assertTrue(paramI0 instanceof BCParamSwitch);		
	}
}
