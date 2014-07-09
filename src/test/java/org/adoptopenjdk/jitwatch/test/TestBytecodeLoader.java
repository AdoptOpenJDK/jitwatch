/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import org.adoptopenjdk.jitwatch.loader.BytecodeLoader;
import org.adoptopenjdk.jitwatch.model.MetaMethod;
import org.adoptopenjdk.jitwatch.model.bytecode.*;
import org.adoptopenjdk.jitwatch.util.ClassUtil;
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

		MemberBytecode memberBytecode = classBytecode.getMemberBytecode(bcSig);

		assertNotNull(memberBytecode);

		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

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

		List<BytecodeInstruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(9, instructions.size());

		BytecodeInstruction i0 = instructions.get(0);
		assertEquals(0, i0.getOffset());
		assertEquals(Opcode.LDC, i0.getOpcode());
		assertEquals(true, i0.hasParameters());
		assertEquals(1, i0.getParameters().size());

		IBytecodeParam paramI0 = i0.getParameters().get(0);
		assertTrue(paramI0 instanceof BCParamConstant);
		assertEquals(224, paramI0.getValue());

		assertEquals(true, i0.hasComment());
		assertEquals("// int 1000000", i0.getComment());

		BytecodeInstruction i1 = instructions.get(1);
		assertEquals(2, i1.getOffset());
		assertEquals(Opcode.ISTORE_1, i1.getOpcode());
		assertEquals(false, i1.hasParameters());
		assertEquals(0, i1.getParameters().size());
		assertEquals(false, i1.hasComment());
		assertEquals(null, i1.getComment());

		BytecodeInstruction i5 = instructions.get(5);
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

		List<BytecodeInstruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(6, instructions.size());

		BytecodeInstruction i4 = instructions.get(4);
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

		List<BytecodeInstruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(6, instructions.size());

		BytecodeInstruction i4 = instructions.get(4);
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

		List<BytecodeInstruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(1, instructions.size());

		BytecodeInstruction i0 = instructions.get(0);
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

		List<BytecodeInstruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(5, instructions.size());

		BytecodeInstruction i3 = instructions.get(3);
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

		List<BytecodeInstruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(5, instructions.size());

		BytecodeInstruction i3 = instructions.get(3);
		assertEquals(3, i3.getOffset());
		assertEquals(Opcode.LOOKUPSWITCH, i3.getOpcode());
		assertEquals(true, i3.hasParameters());
		assertEquals(1, i3.getParameters().size());

		IBytecodeParam paramI0 = i3.getParameters().get(0);
		assertTrue(paramI0 instanceof BCParamSwitch);
	}

	@Test
	public void testLineNumberTable()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Classfile /home/chris/workspace/jw/target/classes/org/adoptopenjdk/jitwatch/demo/SandboxTest.class")
				.append("\n");
		builder.append("  Last modified 18-May-2014; size 426 bytes").append("\n");
		builder.append("  MD5 checksum d8d0af7620175f82d2c5c753b493196f").append("\n");
		builder.append("  Compiled from \"SandboxTest.java\"").append("\n");
		builder.append("public class org.adoptopenjdk.jitwatch.demo.SandboxTest").append("\n");
		builder.append("  SourceFile: \"SandboxTest.java\"").append("\n");
		builder.append("  minor version: 0").append("\n");
		builder.append("  major version: 51").append("\n");
		builder.append("  flags: ACC_PUBLIC, ACC_SUPER").append("\n");
		builder.append("Constant pool:").append("\n");
		builder.append("   #1 = Methodref          #3.#18         //  java/lang/Object.\"<init>\":()V").append("\n");
		builder.append("   #2 = Class              #19            //  org/adoptopenjdk/jitwatch/demo/SandboxTest").append("\n");
		builder.append("   #3 = Class              #20            //  java/lang/Object").append("\n");
		builder.append("   #4 = Utf8               <init>").append("\n");
		builder.append("   #5 = Utf8               ()V").append("\n");
		builder.append("   #6 = Utf8               Code").append("\n");
		builder.append("   #7 = Utf8               LineNumberTable").append("\n");
		builder.append("   #8 = Utf8               LocalVariableTable").append("\n");
		builder.append("   #9 = Utf8               this").append("\n");
		builder.append("  #10 = Utf8               Lorg/adoptopenjdk/jitwatch/demo/SandboxTest;").append("\n");
		builder.append("  #11 = Utf8               add").append("\n");
		builder.append("  #12 = Utf8               (II)I").append("\n");
		builder.append("  #13 = Utf8               a").append("\n");
		builder.append("  #14 = Utf8               I").append("\n");
		builder.append("  #15 = Utf8               b").append("\n");
		builder.append("  #16 = Utf8               SourceFile").append("\n");
		builder.append("  #17 = Utf8               SandboxTest.java").append("\n");
		builder.append("  #18 = NameAndType        #4:#5          //  \"<init>\":()V").append("\n");
		builder.append("  #19 = Utf8               org/adoptopenjdk/jitwatch/demo/SandboxTest").append("\n");
		builder.append("  #20 = Utf8               java/lang/Object").append("\n");
		builder.append("{").append("\n");
		builder.append("  public org.adoptopenjdk.jitwatch.demo.SandboxTest();").append("\n");
		builder.append("    flags: ACC_PUBLIC").append("\n");
		builder.append("    Code:").append("\n");
		builder.append("      stack=1, locals=1, args_size=1").append("\n");
		builder.append("         0: aload_0       ").append("\n");
		builder.append("         1: invokespecial #1                  // Method java/lang/Object.\"<init>\":()V").append("\n");
		builder.append("         4: return        ").append("\n");
		builder.append("      LineNumberTable:").append("\n");
		builder.append("        line 3: 0").append("\n");
		builder.append("      LocalVariableTable:").append("\n");
		builder.append("        Start  Length  Slot  Name   Signature").append("\n");
		builder.append("               0       5     0  this   Lorg/adoptopenjdk/jitwatch/demo/SandboxTest;").append("\n");
		builder.append("").append("\n");
		builder.append("  public int add(int, int);").append("\n");
		builder.append("    flags: ACC_PUBLIC").append("\n");
		builder.append("    Code:").append("\n");
		builder.append("      stack=2, locals=3, args_size=3").append("\n");
		builder.append("         0: iload_1       ").append("\n");
		builder.append("         1: iload_2       ").append("\n");
		builder.append("         2: iadd          ").append("\n");
		builder.append("         3: ireturn       ").append("\n");
		builder.append("      LineNumberTable:").append("\n");
		builder.append("        line 7: 0").append("\n");
		builder.append("      LocalVariableTable:").append("\n");
		builder.append("        Start  Length  Slot  Name   Signature").append("\n");
		builder.append("               0       4     0  this   Lorg/adoptopenjdk/jitwatch/demo/SandboxTest;").append("\n");
		builder.append("               0       4     1     a   I").append("\n");
		builder.append("               0       4     2     b   I").append("\n");
		builder.append("}").append("\n");

		ClassBC classBytecode = BytecodeLoader.parse(builder.toString());

		MemberBytecode memberBytecode = classBytecode.getMemberBytecode("public int add(int,int)");

		assertNotNull(memberBytecode);

		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		assertEquals(4, instructions.size());

		LineTable lineTable = classBytecode.getLineTable();

		assertEquals(2, lineTable.size());

		LineTableEntry entry = lineTable.get(7);

		int offset = entry.getBytecodeOffset();

		assertEquals(0, offset);
		assertEquals("public int add(int,int)", entry.getMemberSignature());

		MemberBytecode memberBytecode2 = classBytecode.getMemberBytecode("public org.adoptopenjdk.jitwatch.demo.SandboxTest()");

		assertNotNull(memberBytecode2);

		List<BytecodeInstruction> instructions2 = memberBytecode2.getInstructions();

		assertEquals(3, instructions2.size());

		LineTableEntry entry2 = lineTable.get(3);

		int offset2 = entry2.getBytecodeOffset();

		assertEquals(0, offset2);
		assertEquals("public org.adoptopenjdk.jitwatch.demo.SandboxTest()", entry2.getMemberSignature());

	}

	@Test
	public void testClassFileVersion()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("public class org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog").append("\n");
		builder.append("SourceFile: \"MakeHotSpotLog.java\"").append("\n");
		builder.append("minor version: 1").append("\n");
		builder.append("major version: 51").append("\n");
		builder.append("flags: ACC_PUBLIC, ACC_SUPER").append("\n");

		ClassBC classBytecode = BytecodeLoader.parse(builder.toString());
		assertEquals(1, classBytecode.getMinorVersion());
		assertEquals(51, classBytecode.getMajorVersion());
	}

	@Test
	public void testClassFileVersionWithRuntimeAnnotations()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("public class org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog").append("\n");
		builder.append("SourceFile: \"MakeHotSpotLog.java\"").append("\n");
		builder.append("  RuntimeVisibleAnnotations:").append("\n");
		builder.append("    0: #49(#50=e#51.#52)").append("\n");
		builder.append("    0: #49(#50=e#51.#52)").append("\n");
		builder.append("    1: #53(#50=[e#54.#55])").append("\n");
		builder.append("    2: #56(#50=e#57.#58)").append("\n");
		builder.append("minor version: 1").append("\n");
		builder.append("major version: 51").append("\n");
		builder.append("flags: ACC_PUBLIC, ACC_SUPER").append("\n");

		ClassBC classBytecode = BytecodeLoader.parse(builder.toString());
		assertEquals(1, classBytecode.getMinorVersion());
		assertEquals(51, classBytecode.getMajorVersion());
	}

	@Test
	public void testRegressionJMHSampleWithRuntimeAnnotations()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("  public void measureWrong();").append("\n");
		builder.append("      descriptor: ()V").append("\n");
		builder.append("    flags: ACC_PUBLIC").append("\n");
		builder.append("    Code:").append("\n");
		builder.append("      stack=2, locals=1, args_size=1").append("\n");
		builder.append("         0: aload_0       ").append("\n");
		builder.append("         0: aload_0       ").append("\n");
		builder.append("         1: getfield      #4          // Field x:D").append("\n");
		builder.append("         4: invokestatic  #5                  // Method java/lang/Math.log:(D)D").append("\n");
		builder.append("         7: pop2          ").append("\n");
		builder.append("         8: return        ").append("\n");
		builder.append("      LineNumberTable:").append("\n");
		builder.append("        line 65: 0").append("\n");
		builder.append("        line 66: 8").append("\n");
		builder.append("      LocalVariableTable:").append("\n");
		builder.append("        Start  Length  Slot  Name   Signature").append("\n");
		builder.append("            0       9     0  this   Lorg/openjdk/jmh/samples/JMHSample_08_DeadCode;").append("\n");
		builder.append("    RuntimeVisibleAnnotations:").append("\n");
		builder.append("      0: #35()").append("\n");
		builder.append("      0: #35()").append("\n");
		
		ClassBC classBytecode = BytecodeLoader.parse(builder.toString());

		MemberBytecode memberBytecode = classBytecode.getMemberBytecode("public void measureWrong()");

		assertNotNull(memberBytecode);

		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		assertEquals(6, instructions.size());
		
		int pos = 0;
		
		BytecodeInstruction i0 = instructions.get(pos++);
		assertEquals(0, i0.getOffset());
		assertEquals(Opcode.ALOAD_0, i0.getOpcode());
		assertEquals(false, i0.hasParameters());
		assertEquals(0, i0.getParameters().size());
		
		BytecodeInstruction i1 = instructions.get(pos++);
		assertEquals(0, i1.getOffset());
		assertEquals(Opcode.ALOAD_0, i1.getOpcode());
		assertEquals(false, i1.hasParameters());
		assertEquals(0, i1.getParameters().size());
		
		BytecodeInstruction i2 = instructions.get(pos++);
		assertEquals(1, i2.getOffset());
		assertEquals(Opcode.GETFIELD, i2.getOpcode());
		assertEquals(true, i2.hasParameters());
		assertEquals(1, i2.getParameters().size());
		
		BytecodeInstruction i3 = instructions.get(pos++);
		assertEquals(4, i3.getOffset());
		assertEquals(Opcode.INVOKESTATIC, i3.getOpcode());
		assertEquals(true, i3.hasParameters());
		assertEquals(1, i3.getParameters().size());
	
		BytecodeInstruction i4 = instructions.get(pos++);
		assertEquals(7, i4.getOffset());
		assertEquals(Opcode.POP2, i4.getOpcode());
		assertEquals(false, i4.hasParameters());
		assertEquals(0, i4.getParameters().size());
		
		BytecodeInstruction i5 = instructions.get(pos++);
		assertEquals(8, i5.getOffset());
		assertEquals(Opcode.RETURN, i5.getOpcode());
		assertEquals(false, i5.hasParameters());
		assertEquals(0, i5.getParameters().size());

		LineTable lineTable = classBytecode.getLineTable();

		assertEquals(2, lineTable.size());

		assertEquals(0, lineTable.get(65).getBytecodeOffset());
		assertEquals(8, lineTable.get(66).getBytecodeOffset());

		assertEquals("public void measureWrong()", lineTable.get(65).getMemberSignature());	
	}
}