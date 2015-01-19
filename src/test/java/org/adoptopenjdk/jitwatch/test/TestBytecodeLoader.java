/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_BYTECODE_STATIC_INITIALISER_SIGNATURE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.loader.BytecodeLoader;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.bytecode.BCParamConstant;
import org.adoptopenjdk.jitwatch.model.bytecode.BCParamNumeric;
import org.adoptopenjdk.jitwatch.model.bytecode.BCParamString;
import org.adoptopenjdk.jitwatch.model.bytecode.BCParamSwitch;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.IBytecodeParam;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTable;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTableEntry;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.junit.Test;

public class TestBytecodeLoader
{
	@Test
	public void testBytecodeSignature()
	{
		String fqClassName = "java.lang.StringBuilder";
		String methodName = "charAt";
		Class<?>[] params = new Class<?>[] { int.class };

		IMetaMember member = UnitTestUtil.createTestMetaMember(fqClassName, methodName, params);

		ClassBC classBytecode = BytecodeLoader.fetchBytecodeForClass(new ArrayList<String>(), fqClassName);

		MemberBytecode memberBytecode = classBytecode.getMemberBytecode(member);

		assertNotNull(memberBytecode);

		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		assertNotNull(instructions);
	}

	@Test
	public void testParseBytecodes()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("0: ldc           #224                // int 1000000").append(S_NEWLINE);
		builder.append("2: istore_1").append(S_NEWLINE);
		builder.append("3: aload_0").append(S_NEWLINE);
		builder.append("4: arraylength").append(S_NEWLINE);
		builder.append("5: iconst_1").append(S_NEWLINE);
		builder.append("6: if_icmpne     28").append(S_NEWLINE);
		builder.append("9: aload_0").append(S_NEWLINE);
		builder.append("10: iconst_0").append(S_NEWLINE);
		builder.append("11: aaload").append(S_NEWLINE);

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
		builder.append("0: lconst_0").append(S_NEWLINE);
		builder.append("1: lstore_2").append(S_NEWLINE);
		builder.append("2: iconst_0").append(S_NEWLINE);
		builder.append("3: lstore    2").append(S_NEWLINE);
		builder.append("4: iinc      4, 1").append(S_NEWLINE);
		builder.append("7: goto      47").append(S_NEWLINE);

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
		builder.append("0: lconst_0").append(S_NEWLINE);
		builder.append("1: lstore_2").append(S_NEWLINE);
		builder.append("2: iconst_0").append(S_NEWLINE);
		builder.append("3: lstore    2").append(S_NEWLINE);
		builder.append("4: iinc      4, -1").append(S_NEWLINE);
		builder.append("7: goto      47").append(S_NEWLINE);

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
		builder.append("3: newarray       int").append(S_NEWLINE);

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
		builder.append("0: lconst_0").append(S_NEWLINE);
		builder.append("1: lstore_2").append(S_NEWLINE);
		builder.append("2: iconst_0").append(S_NEWLINE);
		builder.append("3: tableswitch   { // -1 to 5").append(S_NEWLINE);
		builder.append("             -1: 100").append(S_NEWLINE);
		builder.append("              0: 101").append(S_NEWLINE);
		builder.append("              1: 102").append(S_NEWLINE);
		builder.append("              2: 103").append(S_NEWLINE);
		builder.append("              3: 104").append(S_NEWLINE);
		builder.append("              4: 105").append(S_NEWLINE);
		builder.append("              5: 106").append(S_NEWLINE);
		builder.append("        default: 107").append(S_NEWLINE);
		builder.append("}").append(S_NEWLINE);
		builder.append("99: lstore_2").append(S_NEWLINE);

		List<BytecodeInstruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(5, instructions.size());

		BytecodeInstruction i3 = instructions.get(3);
		assertEquals(3, i3.getOffset());
		assertEquals(Opcode.TABLESWITCH, i3.getOpcode());
		assertEquals(true, i3.hasParameters());
		assertEquals(1, i3.getParameters().size());

		IBytecodeParam paramI0 = i3.getParameters().get(0);
		assertTrue(paramI0 instanceof BCParamSwitch);

		BytecodeInstruction i99 = instructions.get(4);
		assertEquals(99, i99.getOffset());
		assertEquals(Opcode.LSTORE_2, i99.getOpcode());
		assertEquals(false, i99.hasParameters());
	}

	@Test
	public void testParseBytecodeRegressionTableSwitch2()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("39: ldc           #8                  // int 1000000").append(S_NEWLINE);
		builder.append("41: if_icmpge     104").append(S_NEWLINE);
		builder.append("44: iload         5").append(S_NEWLINE);
		builder.append("46: iconst_3").append(S_NEWLINE);
		builder.append("47: irem          ").append(S_NEWLINE);
		builder.append("48: tableswitch   { // 0 to 2").append(S_NEWLINE);
		builder.append("             0: 76").append(S_NEWLINE);
		builder.append("             1: 82").append(S_NEWLINE);
		builder.append("             2: 88").append(S_NEWLINE);
		builder.append("       default: 91").append(S_NEWLINE);
		builder.append("  }").append(S_NEWLINE);
		builder.append("76: aload_1       ").append(S_NEWLINE);
		builder.append("77: astore        4").append(S_NEWLINE);
		builder.append("79: goto          91").append(S_NEWLINE);

		List<BytecodeInstruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		assertEquals(9, instructions.size());

		BytecodeInstruction i47 = instructions.get(4);
		assertEquals(47, i47.getOffset());
		assertEquals(Opcode.IREM, i47.getOpcode());
		assertEquals(false, i47.hasParameters());

		BytecodeInstruction i76 = instructions.get(6);
		assertEquals(76, i76.getOffset());
		assertEquals(Opcode.ALOAD_1, i76.getOpcode());
		assertEquals(false, i76.hasParameters());
	}

	@Test
	public void testParseBytecodeRegressionLookupSwitch()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("0: lconst_0").append(S_NEWLINE);
		builder.append("1: lstore_2").append(S_NEWLINE);
		builder.append("2: iconst_0").append(S_NEWLINE);
		builder.append("3: lookupswitch   { // -1 to 5").append(S_NEWLINE);
		builder.append("             -1: 100").append(S_NEWLINE);
		builder.append("              0: 101").append(S_NEWLINE);
		builder.append("              1: 102").append(S_NEWLINE);
		builder.append("              2: 103").append(S_NEWLINE);
		builder.append("              3: 104").append(S_NEWLINE);
		builder.append("              4: 105").append(S_NEWLINE);
		builder.append("              5: 106").append(S_NEWLINE);
		builder.append("        default: 107").append(S_NEWLINE);
		builder.append("}").append(S_NEWLINE);
		builder.append("99: lstore_2").append(S_NEWLINE);

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

	// used in next test

	public int add(int x, int y)
	{
		return x + y;
	}

	@Test
	public void testLineNumberTable() throws ClassNotFoundException
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Classfile TestBytecodeLoader.class").append(S_NEWLINE);
		builder.append("  Last modified 18-May-2014; size 426 bytes").append(S_NEWLINE);
		builder.append("  MD5 checksum d8d0af7620175f82d2c5c753b493196f").append(S_NEWLINE);
		builder.append("  Compiled from \"TestBytecodeLoader.java\"").append(S_NEWLINE);
		builder.append("public class org.adoptopenjdk.jitwatch.test.TestBytecodeLoader").append(S_NEWLINE);
		builder.append("  SourceFile: \"TestBytecodeLoader.java\"").append(S_NEWLINE);
		builder.append("  minor version: 0").append(S_NEWLINE);
		builder.append("  major version: 51").append(S_NEWLINE);
		builder.append("  flags: ACC_PUBLIC, ACC_SUPER").append(S_NEWLINE);
		builder.append("Constant pool:").append(S_NEWLINE);
		builder.append("   #1 = Methodref          #3.#18         //  java/lang/Object.\"<init>\":()V").append(S_NEWLINE);
		builder.append("   #2 = Class              #19            //  org/adoptopenjdk/jitwatch/test/TestBytecodeLoader").append(
				S_NEWLINE);
		builder.append("   #3 = Class              #20            //  java/lang/Object").append(S_NEWLINE);
		builder.append("   #4 = Utf8               <init>").append(S_NEWLINE);
		builder.append("   #5 = Utf8               ()V").append(S_NEWLINE);
		builder.append("   #6 = Utf8               Code").append(S_NEWLINE);
		builder.append("   #7 = Utf8               LineNumberTable").append(S_NEWLINE);
		builder.append("   #8 = Utf8               LocalVariableTable").append(S_NEWLINE);
		builder.append("   #9 = Utf8               this").append(S_NEWLINE);
		builder.append("  #10 = Utf8               Lorg/adoptopenjdk/jitwatch/test/TestBytecodeLoader;").append(S_NEWLINE);
		builder.append("  #11 = Utf8               add").append(S_NEWLINE);
		builder.append("  #12 = Utf8               (II)I").append(S_NEWLINE);
		builder.append("  #13 = Utf8               a").append(S_NEWLINE);
		builder.append("  #14 = Utf8               I").append(S_NEWLINE);
		builder.append("  #15 = Utf8               b").append(S_NEWLINE);
		builder.append("  #16 = Utf8               SourceFile").append(S_NEWLINE);
		builder.append("  #17 = Utf8               TestBytecodeLoader.java").append(S_NEWLINE);
		builder.append("  #18 = NameAndType        #4:#5          //  \"<init>\":()V").append(S_NEWLINE);
		builder.append("  #19 = Utf8               org/adoptopenjdk/jitwatch/test/TestBytecodeLoader").append(S_NEWLINE);
		builder.append("  #20 = Utf8               java/lang/Object").append(S_NEWLINE);
		builder.append("{").append(S_NEWLINE);
		builder.append("  public org.adoptopenjdk.jitwatch.test.TestBytecodeLoader();").append(S_NEWLINE);
		builder.append("    flags: ACC_PUBLIC").append(S_NEWLINE);
		builder.append("    Code:").append(S_NEWLINE);
		builder.append("      stack=1, locals=1, args_size=1").append(S_NEWLINE);
		builder.append("         0: aload_0       ").append(S_NEWLINE);
		builder.append("         1: invokespecial #1                  // Method java/lang/Object.\"<init>\":()V").append(S_NEWLINE);
		builder.append("         4: return        ").append(S_NEWLINE);
		builder.append("      LineNumberTable:").append(S_NEWLINE);
		builder.append("        line 3: 0").append(S_NEWLINE);
		builder.append("      LocalVariableTable:").append(S_NEWLINE);
		builder.append("        Start  Length  Slot  Name   Signature").append(S_NEWLINE);
		builder.append("               0       5     0  this   Lorg/adoptopenjdk/jitwatch/test/TestBytecodeLoader;").append(
				S_NEWLINE);
		builder.append("").append(S_NEWLINE);
		builder.append("  public int add(int, int);").append(S_NEWLINE);
		builder.append("    flags: ACC_PUBLIC").append(S_NEWLINE);
		builder.append("    Code:").append(S_NEWLINE);
		builder.append("      stack=2, locals=3, args_size=3").append(S_NEWLINE);
		builder.append("         0: iload_1       ").append(S_NEWLINE);
		builder.append("         1: iload_2       ").append(S_NEWLINE);
		builder.append("         2: iadd          ").append(S_NEWLINE);
		builder.append("         3: ireturn       ").append(S_NEWLINE);
		builder.append("      LineNumberTable:").append(S_NEWLINE);
		builder.append("        line 7: 0").append(S_NEWLINE);
		builder.append("      LocalVariableTable:").append(S_NEWLINE);
		builder.append("        Start  Length  Slot  Name   Signature").append(S_NEWLINE);
		builder.append("               0       4     0  this   Lorg/adoptopenjdk/jitwatch/test/TestBytecodeLoader;").append(
				S_NEWLINE);
		builder.append("               0       4     1     a   I").append(S_NEWLINE);
		builder.append("               0       4     2     b   I").append(S_NEWLINE);
		builder.append("}").append(S_NEWLINE);

		IMetaMember member = UnitTestUtil
				.createTestMetaMember(getClass().getName(), "add", new Class<?>[] { int.class, int.class });

		ClassBC classBytecode = BytecodeLoader.parse(getClass().getName(), builder.toString());

		MemberBytecode memberBytecode = classBytecode.getMemberBytecode(member);

		assertNotNull(memberBytecode);

		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		assertEquals(4, instructions.size());

		LineTable lineTable = memberBytecode.getLineTable();

		assertEquals(1, lineTable.size());

		LineTableEntry entry = lineTable.getEntryForSourceLine(7);

		int offset = entry.getBytecodeOffset();

		assertEquals(0, offset);

		JITDataModel model = new JITDataModel();

		MetaClass metaClass = UnitTestUtil.createMetaClassFor(model, getClass().getName());

		IMetaMember constructor = metaClass.getFirstConstructor();

		MemberBytecode memberBytecode2 = classBytecode.getMemberBytecode(constructor);

		assertNotNull(memberBytecode2);

		List<BytecodeInstruction> instructions2 = memberBytecode2.getInstructions();

		assertEquals(3, instructions2.size());

		LineTable lineTable2 = memberBytecode2.getLineTable();

		LineTableEntry entry2 = lineTable2.getEntryForSourceLine(3);

		int offset2 = entry2.getBytecodeOffset();

		assertEquals(0, offset2);
	}

	@Test
	public void testClassFileVersion()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("public class org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog").append(S_NEWLINE);
		builder.append("SourceFile: \"MakeHotSpotLog.java\"").append(S_NEWLINE);
		builder.append("minor version: 1").append(S_NEWLINE);
		builder.append("major version: 51").append(S_NEWLINE);
		builder.append("flags: ACC_PUBLIC, ACC_SUPER").append(S_NEWLINE);

		ClassBC classBytecode = BytecodeLoader.parse("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", builder.toString());
		assertEquals(1, classBytecode.getMinorVersion());
		assertEquals(51, classBytecode.getMajorVersion());
	}

	@Test
	public void testClassFileVersionWithRuntimeAnnotations()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("public class org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog").append(S_NEWLINE);
		builder.append("SourceFile: \"MakeHotSpotLog.java\"").append(S_NEWLINE);
		builder.append("  RuntimeVisibleAnnotations:").append(S_NEWLINE);
		builder.append("    0: #49(#50=e#51.#52)").append(S_NEWLINE);
		builder.append("    0: #49(#50=e#51.#52)").append(S_NEWLINE);
		builder.append("    1: #53(#50=[e#54.#55])").append(S_NEWLINE);
		builder.append("    2: #56(#50=e#57.#58)").append(S_NEWLINE);
		builder.append("minor version: 1").append(S_NEWLINE);
		builder.append("major version: 51").append(S_NEWLINE);
		builder.append("flags: ACC_PUBLIC, ACC_SUPER").append(S_NEWLINE);

		ClassBC classBytecode = BytecodeLoader.parse("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", builder.toString());
		assertEquals(1, classBytecode.getMinorVersion());
		assertEquals(51, classBytecode.getMajorVersion());
	}

	@Test
	public void testRegressionJMHSampleWithRuntimeAnnotations()
	{
		StringBuilder builder = new StringBuilder();

		String bcSig = "public void measureWrong();";

		builder.append("  ").append(bcSig).append(S_NEWLINE);
		builder.append("      descriptor: ()V").append(S_NEWLINE);
		builder.append("    flags: ACC_PUBLIC").append(S_NEWLINE);
		builder.append("    Code:").append(S_NEWLINE);
		builder.append("      stack=2, locals=1, args_size=1").append(S_NEWLINE);
		builder.append("         0: aload_0       ").append(S_NEWLINE);
		builder.append("         0: aload_0       ").append(S_NEWLINE);
		builder.append("         1: getfield      #4          // Field x:D").append(S_NEWLINE);
		builder.append("         4: invokestatic  #5                  // Method java/lang/Math.log:(D)D").append(S_NEWLINE);
		builder.append("         7: pop2          ").append(S_NEWLINE);
		builder.append("         8: return        ").append(S_NEWLINE);
		builder.append("      LineNumberTable:").append(S_NEWLINE);
		builder.append("        line 65: 0").append(S_NEWLINE);
		builder.append("        line 66: 8").append(S_NEWLINE);
		builder.append("      LocalVariableTable:").append(S_NEWLINE);
		builder.append("        Start  Length  Slot  Name   Signature").append(S_NEWLINE);
		builder.append("            0       9     0  this   Lorg/openjdk/jmh/samples/JMHSample_08_DeadCode;").append(S_NEWLINE);
		builder.append("    RuntimeVisibleAnnotations:").append(S_NEWLINE);
		builder.append("      0: #35()").append(S_NEWLINE);
		builder.append("      0: #35()").append(S_NEWLINE);

		ClassBC classBytecode = BytecodeLoader.parse(getClass().getName(), builder.toString());

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature(getClass().getName(), bcSig);

		MemberBytecode memberBytecode = classBytecode.getMemberBytecodeForSignature(msp);

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

		LineTable lineTable = memberBytecode.getLineTable();

		assertEquals(2, lineTable.size());

		assertEquals(0, lineTable.getEntryForSourceLine(65).getBytecodeOffset());
		assertEquals(8, lineTable.getEntryForSourceLine(66).getBytecodeOffset());

		assertEquals(65, lineTable.findSourceLineForBytecodeOffset(0));
		assertEquals(65, lineTable.findSourceLineForBytecodeOffset(1));
		assertEquals(65, lineTable.findSourceLineForBytecodeOffset(2));
		assertEquals(65, lineTable.findSourceLineForBytecodeOffset(3));
		assertEquals(65, lineTable.findSourceLineForBytecodeOffset(4));
		assertEquals(65, lineTable.findSourceLineForBytecodeOffset(5));
		assertEquals(65, lineTable.findSourceLineForBytecodeOffset(6));
		assertEquals(65, lineTable.findSourceLineForBytecodeOffset(7));
		assertEquals(66, lineTable.findSourceLineForBytecodeOffset(8));
		assertEquals(66, lineTable.findSourceLineForBytecodeOffset(9));
		assertEquals(66, lineTable.findSourceLineForBytecodeOffset(100));
	}

	@Test
	public void testStaticInitialiserRegression()
	{
		String[] lines = new String[] {
				"  static {};",
				"                        descriptor: ()V",
				"                        flags: ACC_STATIC",
				"                        Code:",
				"                          stack=3, locals=0, args_size=0",
				"                             0: ldc           #149                // class org/adoptopenjdk/jitwatch/loader/BytecodeLoader",
				"                             2: invokestatic  #150                // Method org/slf4j/LoggerFactory.getLogger:(Ljava/lang/Class;)Lorg/slf4j/Logger;",
				"                             5: putstatic     #33                 // Field logger:Lorg/slf4j/Logger;",
				"                             8: new           #151                // class java/util/HashMap",
				"                            11: dup",
				"                            12: invokespecial #152                // Method java/util/HashMap.\"<init>\":()V",
				"                            15: putstatic     #90                 // Field sectionLabelMap:Ljava/util/Map;",
				"                            18: getstatic     #90                 // Field sectionLabelMap:Ljava/util/Map;",
				"                            21: ldc           #153                // String Constant pool:",
				"                            23: getstatic     #69                 // Field org/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection.CONSTANT_POOL:Lorg/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection;",
				"                            26: invokeinterface #154,  3          // InterfaceMethod java/util/Map.put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
				"                            31: pop",
				"                            32: getstatic     #90                 // Field sectionLabelMap:Ljava/util/Map;",
				"                            35: ldc           #155                // String Code:",
				"                            37: getstatic     #67                 // Field org/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection.CODE:Lorg/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection;",
				"                            40: invokeinterface #154,  3          // InterfaceMethod java/util/Map.put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
				"                            45: pop",
				"                            46: getstatic     #90                 // Field sectionLabelMap:Ljava/util/Map;",
				"                            49: ldc           #156                // String LineNumberTable:",
				"                            51: getstatic     #71                 // Field org/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection.LINETABLE:Lorg/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection;",
				"                            54: invokeinterface #154,  3          // InterfaceMethod java/util/Map.put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
				"                            59: pop",
				"                            60: getstatic     #90                 // Field sectionLabelMap:Ljava/util/Map;",
				"                            63: ldc           #157                // String LocalVariableTable:",
				"                            65: getstatic     #158                // Field org/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection.LOCALVARIABLETABLE:Lorg/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection;",
				"                            68: invokeinterface #154,  3          // InterfaceMethod java/util/Map.put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
				"                            73: pop",
				"                            74: getstatic     #90                 // Field sectionLabelMap:Ljava/util/Map;",
				"                            77: ldc           #159                // String RuntimeVisibleAnnotations:",
				"                            79: getstatic     #160                // Field org/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection.RUNTIMEVISIBLEANNOTATIONS:Lorg/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSecti$",
				"                            82: invokeinterface #154,  3          // InterfaceMethod java/util/Map.put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
				"                            87: pop",
				"                            88: getstatic     #90                 // Field sectionLabelMap:Ljava/util/Map;",
				"                            91: ldc           #161                // String Exceptions:",
				"                            93: getstatic     #162                // Field org/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection.EXCEPTIONS:Lorg/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection;",
				"                            96: invokeinterface #154,  3          // InterfaceMethod java/util/Map.put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
				"                           101: pop",
				"                           102: getstatic     #90                 // Field sectionLabelMap:Ljava/util/Map;",
				"                           105: ldc           #163                // String StackMapTable:",
				"                           107: getstatic     #164                // Field org/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection.STACKMAPTABLE:Lorg/adoptopenjdk/jitwatch/loader/BytecodeLoader$BytecodeSection;",
				"                           110: invokeinterface #154,  3          // InterfaceMethod java/util/Map.put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
				"                           115: pop",
				"                           116: return",
				"                          LineNumberTable:",
				"                            line 30: 0",
				"                            line 37: 8",
				"                            line 41: 18",
				"                            line 42: 32",
				"                            line 43: 46",
				"                            line 44: 60",
				"                            line 45: 74" };

		StringBuilder builder = new StringBuilder();

		for (String line : lines)
		{
			builder.append(line).append(C_NEWLINE);
		}

		ClassBC classBytecode = BytecodeLoader.parse(getClass().getName(), builder.toString());

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature(getClass().getName(),
				S_BYTECODE_STATIC_INITIALISER_SIGNATURE);

		MemberBytecode memberBytecode = classBytecode.getMemberBytecodeForSignature(msp);

		assertNotNull(memberBytecode);

		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		assertEquals(43, instructions.size());
	}

	@Test
	public void testRegressionLoadJavaIoPrintStreamString()
	{
		String className = "java.io.PrintStream";
		String methodName = "print";

		IMetaMember member = UnitTestUtil.createTestMetaMember(className, methodName, new Class<?>[] { java.lang.String.class });

		ClassBC classBytecode = BytecodeLoader.fetchBytecodeForClass(new ArrayList<String>(), className);

		MemberBytecode memberBytecode = classBytecode.getMemberBytecode(member);

		assertNotNull(memberBytecode);

		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		assertNotNull(instructions);

		assertEquals(8, instructions.size());
	}

	@Test
	public void testRegressionLoadJavaIoPrintStreamObject()
	{
		String className = "java.io.PrintStream";
		String methodName = "print";

		IMetaMember member = UnitTestUtil.createTestMetaMember(className, methodName, new Class<?>[] { java.lang.Object.class });

		ClassBC classBytecode = BytecodeLoader.fetchBytecodeForClass(new ArrayList<String>(), className);

		MemberBytecode memberBytecode = classBytecode.getMemberBytecode(member);

		assertNotNull(memberBytecode);

		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		assertNotNull(instructions);

		assertEquals(5, instructions.size());
	}

	@Test
	public void testSignatureGenericsParser()
	{
		String line = "Signature: #259                         // <K:Ljava/lang/Integer;V:Ljava/lang/String;>Ljava/util/AbstractMap<TK;TV;>;Ljava/util/concurrent/ConcurrentMap<TK;TV;>;Ljava/io/Serializable;";

		ClassBC classBytecode = new ClassBC();

		BytecodeLoader.buildClassGenerics(line, classBytecode);

		assertEquals(2, classBytecode.getGenericsMap().size());
		assertTrue(classBytecode.getGenericsMap().containsKey("K"));
		assertEquals("java.lang.Integer", classBytecode.getGenericsMap().get("K"));
		assertTrue(classBytecode.getGenericsMap().containsKey("V"));
		assertEquals("java.lang.String", classBytecode.getGenericsMap().get("V"));
	}

	@Test
	public void testInnerClassNameFinder()
	{
		String line1 = "public #13= #6 of #10; //Cow=class PolymorphismTest$Cow of class PolymorphismTest";
		String line2 = "public #15= #4 of #10; //Cat=class PolymorphismTest$Cat of class PolymorphismTest";
		String line3 = "public #16= #2 of #10; //Dog=class PolymorphismTest$Dog of class PolymorphismTest";
		String line4 = "public static #18= #17 of #10; //Animal=class PolymorphismTest$Animal of class PolymorphismTest";
		String line5 = "foo";

		assertEquals("PolymorphismTest$Cow", BytecodeLoader.getInnerClassNameOrNull(line1));
		assertEquals("PolymorphismTest$Cat", BytecodeLoader.getInnerClassNameOrNull(line2));
		assertEquals("PolymorphismTest$Dog", BytecodeLoader.getInnerClassNameOrNull(line3));
		assertEquals("PolymorphismTest$Animal", BytecodeLoader.getInnerClassNameOrNull(line4));
		assertEquals(null, BytecodeLoader.getInnerClassNameOrNull(line5));
	}

	@Test
	public void testClassFileVersionWithInnerClasses()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("public class org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog").append(S_NEWLINE);
		builder.append("SourceFile: \"MakeHotSpotLog.java\"").append(S_NEWLINE);
		builder.append("InnerClasses:").append(S_NEWLINE);
		builder.append("public #13= #6 of #10; //Cow=class PolymorphismTest$Cow of class PolymorphismTest").append(S_NEWLINE);
		builder.append("public #15= #4 of #10; //Cat=class PolymorphismTest$Cat of class PolymorphismTest").append(S_NEWLINE);
		builder.append("public #16= #2 of #10; //Dog=class PolymorphismTest$Dog of class PolymorphismTest").append(S_NEWLINE);
		builder.append("public static #18= #17 of #10; //Animal=class PolymorphismTest$Animal of class PolymorphismTest").append(
				S_NEWLINE);

		builder.append("minor version: 1").append(S_NEWLINE);
		builder.append("major version: 51").append(S_NEWLINE);
		builder.append("flags: ACC_PUBLIC, ACC_SUPER").append(S_NEWLINE);

		ClassBC classBytecode = BytecodeLoader.parse("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", builder.toString());
		assertEquals(1, classBytecode.getMinorVersion());
		assertEquals(51, classBytecode.getMajorVersion());

		assertEquals(4, classBytecode.getInnerClassNames().size());
	}
}