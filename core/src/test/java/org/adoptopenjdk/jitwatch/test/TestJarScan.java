/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.jarscan.sequencecount.InstructionSequence;
import org.adoptopenjdk.jitwatch.jarscan.sequencecount.SequenceCountOperation;
import org.adoptopenjdk.jitwatch.loader.BytecodeLoader;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.junit.Test;

public class TestJarScan
{
	public static final boolean SHOW_OUTPUT = false;
	
	private List<BytecodeInstruction> getInstructions(String[] lines)
	{
		StringBuilder builder = new StringBuilder();

		for (String line : lines)
		{
			builder.append(line).append("\n");
		}

		List<BytecodeInstruction> instructions = BytecodeLoader.parseInstructions(builder.toString());

		return instructions;
	}

	private MemberBytecode createMemberBytecode(String[] lines)
	{
		MemberBytecode mbc = new MemberBytecode(null, null);

		mbc.setInstructions(getInstructions(lines));

		return mbc;
	}

	@Test
	public void testLongBytecodeChain1()
	{
		String[] lines = new String[] {
				"0: aload_1",
				"1: invokevirtual #38                 // Method org/adoptopenjdk/jitwatch/model/Tag.getName:()Ljava/lang/String;",
				"4: astore_3",
				"5: iconst_m1",
				"6: istore        4",
				"8: aload_3",
				"9: invokevirtual #39                 // Method java/lang/String.hashCode:()I",
				"12: lookupswitch  { // 3",
				"   -1067517155: 63",
				"     106437299: 48",
				"     922165544: 78",
				"       default: 90",
				"  }",
				"48: aload_3",
				"49: ldc           #14                 // String parse",
				"51: invokevirtual #40                 // Method java/lang/String.equals:(Ljava/lang/Object;)Z",
				"54: ifeq          90",
				"57: iconst_0",
				"58: istore        4",
				"60: goto          90",
				"63: aload_3",
				"64: ldc           #41                 // String eliminate_allocation",
				"66: invokevirtual #40                 // Method java/lang/String.equals:(Ljava/lang/Object;)Z",
				"69: ifeq          90",
				"72: iconst_1",
				"73: istore        4",
				"75: goto          90",
				"78: aload_3",
				"79: ldc           #42                 // String eliminate_lock",
				"81: invokevirtual #40                 // Method java/lang/String.equals:(Ljava/lang/Object;)Z",
				"84: ifeq          90",
				"87: iconst_2",
				"88: istore        4",
				"90: iload         4",
				"92: tableswitch   { // 0 to 2",
				"             0: 120",
				"             1: 129",
				"             2: 138",
				"       default: 147",
				"  }",
				"120: aload_0",
				"121: aload_1",
				"122: aload_2",
				"123: invokespecial #43                 // Method visitTagParse:(Lorg/adoptopenjdk/jitwatch/model/Tag;Lorg/adoptopenjdk/jitwatch/model/IParseDictionary;)V",
				"126: goto          152",
				"129: aload_0",
				"130: aload_1",
				"131: aload_2",
				"132: invokespecial #44                 // Method visitTagEliminateAllocation:(Lorg/adoptopenjdk/jitwatch/model/Tag;Lorg/adoptopenjdk/jitwatch/model/IParseDictionary;)V",
				"135: goto          152",
				"138: aload_0",
				"139: aload_1",
				"140: aload_2",
				"141: invokespecial #45                 // Method visitTagEliminateLock:(Lorg/adoptopenjdk/jitwatch/model/Tag;Lorg/adoptopenjdk/jitwatch/model/IParseDictionary;)V",
				"144: goto          152",
				"147: aload_0",
				"148: aload_1",
				"149: invokevirtual #46                 // Method handleOther:(Lorg/adoptopenjdk/jitwatch/model/Tag;)V",
				"152: return"
		};

		MemberBytecode memberBytecode = createMemberBytecode(lines);

		SequenceCountOperation counter = new SequenceCountOperation(1);

		counter.processInstructions("Foo", memberBytecode);

		Map<InstructionSequence, Integer> result = counter.getSequenceScores();

		log(result);

		assertEquals(19, result.size());

		checkSequence(result, 5, Opcode.ALOAD_1);
		checkSequence(result, 6, Opcode.INVOKEVIRTUAL);
		checkSequence(result, 1, Opcode.ASTORE_3);
		checkSequence(result, 1, Opcode.ICONST_M1);
		checkSequence(result, 4, Opcode.ISTORE);
		checkSequence(result, 4, Opcode.ALOAD_3);
		checkSequence(result, 1, Opcode.LOOKUPSWITCH);
		checkSequence(result, 3, Opcode.LDC);
		checkSequence(result, 3, Opcode.IFEQ);
		checkSequence(result, 1, Opcode.ICONST_0);
		checkSequence(result, 1, Opcode.ICONST_1);
		checkSequence(result, 1, Opcode.ICONST_2);
		checkSequence(result, 1, Opcode.ILOAD);
		checkSequence(result, 1, Opcode.TABLESWITCH);
		checkSequence(result, 4, Opcode.ALOAD_0);
		checkSequence(result, 3, Opcode.ALOAD_2);
		checkSequence(result, 3, Opcode.INVOKESPECIAL);
		checkSequence(result, 5, Opcode.GOTO);
		checkSequence(result, 1, Opcode.RETURN);
	}

	@Test
	public void testLongBytecodeChain6()
	{
		String[] lines = new String[] {
				"0: aload_1",
				"1: invokevirtual #38                 // Method org/adoptopenjdk/jitwatch/model/Tag.getName:()Ljava/lang/String;",
				"4: astore_3",
				"5: iconst_m1",
				"6: istore        4",
				"8: aload_3",
				"9: invokevirtual #39                 // Method java/lang/String.hashCode:()I",
				"12: lookupswitch  { // 3",
				"   -1067517155: 63",
				"     106437299: 48",
				"     922165544: 78",
				"       default: 90",
				"  }",
				"48: aload_3",
				"49: ldc           #14                 // String parse",
				"51: invokevirtual #40                 // Method java/lang/String.equals:(Ljava/lang/Object;)Z",
				"54: ifeq          90",
				"57: iconst_0",
				"58: istore        4",
				"60: goto          90",
				"63: aload_3",
				"64: ldc           #41                 // String eliminate_allocation",
				"66: invokevirtual #40                 // Method java/lang/String.equals:(Ljava/lang/Object;)Z",
				"69: ifeq          90",
				"72: iconst_1",
				"73: istore        4",
				"75: goto          90",
				"78: aload_3",
				"79: ldc           #42                 // String eliminate_lock",
				"81: invokevirtual #40                 // Method java/lang/String.equals:(Ljava/lang/Object;)Z",
				"84: ifeq          90",
				"87: iconst_2",
				"88: istore        4",
				"90: iload         4",
				"92: tableswitch   { // 0 to 2",
				"             0: 120",
				"             1: 129",
				"             2: 138",
				"       default: 147",
				"  }",
				"120: aload_0",
				"121: aload_1",
				"122: aload_2",
				"123: invokespecial #43                 // Method visitTagParse:(Lorg/adoptopenjdk/jitwatch/model/Tag;Lorg/adoptopenjdk/jitwatch/model/IParseDictionary;)V",
				"126: goto          152",
				"129: aload_0",
				"130: aload_1",
				"131: aload_2",
				"132: invokespecial #44                 // Method visitTagEliminateAllocation:(Lorg/adoptopenjdk/jitwatch/model/Tag;Lorg/adoptopenjdk/jitwatch/model/IParseDictionary;)V",
				"135: goto          152",
				"138: aload_0",
				"139: aload_1",
				"140: aload_2",
				"141: invokespecial #45                 // Method visitTagEliminateLock:(Lorg/adoptopenjdk/jitwatch/model/Tag;Lorg/adoptopenjdk/jitwatch/model/IParseDictionary;)V",
				"144: goto          152",
				"147: aload_0",
				"148: aload_1",
				"149: invokevirtual #46                 // Method handleOther:(Lorg/adoptopenjdk/jitwatch/model/Tag;)V",
				"152: return"
		};

		MemberBytecode memberBytecode = createMemberBytecode(lines);

		SequenceCountOperation counter = new SequenceCountOperation(6);

		counter.processInstructions("Foo", memberBytecode);

		Map<InstructionSequence, Integer> result = counter.getSequenceScores();

		log(result);

		assertEquals(29, result.size());

		checkSequence(result, 1, Opcode.ALOAD_1, Opcode.INVOKEVIRTUAL, Opcode.ASTORE_3, Opcode.ICONST_M1, Opcode.ISTORE, Opcode.ALOAD_3);
		checkSequence(result, 1, Opcode.INVOKEVIRTUAL, Opcode.ASTORE_3, Opcode.ICONST_M1, Opcode.ISTORE, Opcode.ALOAD_3, Opcode.INVOKEVIRTUAL);
		checkSequence(result, 1, Opcode.ASTORE_3, Opcode.ICONST_M1, Opcode.ISTORE, Opcode.ALOAD_3, Opcode.INVOKEVIRTUAL, Opcode.LOOKUPSWITCH);
		checkSequence(result, 1, Opcode.ICONST_M1, Opcode.ISTORE, Opcode.ALOAD_3, Opcode.INVOKEVIRTUAL, Opcode.LOOKUPSWITCH, Opcode.ALOAD_3);
		checkSequence(result, 1, Opcode.ISTORE, Opcode.ALOAD_3, Opcode.INVOKEVIRTUAL, Opcode.LOOKUPSWITCH, Opcode.ALOAD_3, Opcode.LDC);
		checkSequence(result, 1, Opcode.ALOAD_3, Opcode.INVOKEVIRTUAL, Opcode.LOOKUPSWITCH, Opcode.ALOAD_3, Opcode.LDC, Opcode.INVOKEVIRTUAL);
		checkSequence(result, 1, Opcode.INVOKEVIRTUAL, Opcode.LOOKUPSWITCH, Opcode.ALOAD_3, Opcode.LDC, Opcode.INVOKEVIRTUAL, Opcode.IFEQ);
		checkSequence(result, 1, Opcode.LOOKUPSWITCH, Opcode.ALOAD_3, Opcode.LDC, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.ICONST_0);
		checkSequence(result, 1, Opcode.ALOAD_3, Opcode.LDC, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.ICONST_0, Opcode.ISTORE);
		checkSequence(result, 1, Opcode.LDC, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.ICONST_0, Opcode.ISTORE, Opcode.GOTO);
		checkSequence(result, 1, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.ICONST_0, Opcode.ISTORE, Opcode.GOTO, Opcode.ILOAD);
		checkSequence(result, 1, Opcode.IFEQ, Opcode.ICONST_0, Opcode.ISTORE, Opcode.GOTO, Opcode.ILOAD, Opcode.TABLESWITCH);
		checkSequence(result, 1, Opcode.ICONST_0, Opcode.ISTORE, Opcode.GOTO, Opcode.ILOAD, Opcode.TABLESWITCH, Opcode.ALOAD_0);
		checkSequence(result, 2, Opcode.ISTORE, Opcode.GOTO, Opcode.ILOAD, Opcode.TABLESWITCH, Opcode.ALOAD_0, Opcode.ALOAD_1);
		checkSequence(result, 2, Opcode.GOTO, Opcode.ILOAD, Opcode.TABLESWITCH, Opcode.ALOAD_0, Opcode.ALOAD_1, Opcode.ALOAD_2);
		checkSequence(result, 1, Opcode.ILOAD, Opcode.TABLESWITCH, Opcode.ALOAD_0, Opcode.ALOAD_1, Opcode.ALOAD_2, Opcode.INVOKESPECIAL);
		checkSequence(result, 1, Opcode.TABLESWITCH, Opcode.ALOAD_0, Opcode.ALOAD_1, Opcode.ALOAD_2, Opcode.INVOKESPECIAL, Opcode.GOTO);
		checkSequence(result, 3, Opcode.ALOAD_0, Opcode.ALOAD_1, Opcode.ALOAD_2, Opcode.INVOKESPECIAL, Opcode.GOTO, Opcode.RETURN);
		checkSequence(result, 1, Opcode.ALOAD_3, Opcode.LDC, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.ICONST_1, Opcode.ISTORE);
		checkSequence(result, 1, Opcode.LDC, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.ICONST_1, Opcode.ISTORE, Opcode.GOTO);
		checkSequence(result, 1, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.ICONST_1, Opcode.ISTORE, Opcode.GOTO, Opcode.ILOAD);
		checkSequence(result, 1, Opcode.IFEQ, Opcode.ICONST_1, Opcode.ISTORE, Opcode.GOTO, Opcode.ILOAD, Opcode.TABLESWITCH);
		checkSequence(result, 1, Opcode.ICONST_1, Opcode.ISTORE, Opcode.GOTO, Opcode.ILOAD, Opcode.TABLESWITCH, Opcode.ALOAD_0);
		checkSequence(result, 1, Opcode.ALOAD_3, Opcode.LDC, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.ICONST_2, Opcode.ISTORE);
		checkSequence(result, 1, Opcode.LDC, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.ICONST_2, Opcode.ISTORE, Opcode.ILOAD);
		checkSequence(result, 1, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.ICONST_2, Opcode.ISTORE, Opcode.ILOAD, Opcode.TABLESWITCH);
		checkSequence(result, 1, Opcode.IFEQ, Opcode.ICONST_2, Opcode.ISTORE, Opcode.ILOAD, Opcode.TABLESWITCH, Opcode.ALOAD_0);
		checkSequence(result, 1, Opcode.ICONST_2, Opcode.ISTORE, Opcode.ILOAD, Opcode.TABLESWITCH, Opcode.ALOAD_0, Opcode.ALOAD_1);
		checkSequence(result, 1, Opcode.ISTORE, Opcode.ILOAD, Opcode.TABLESWITCH, Opcode.ALOAD_0, Opcode.ALOAD_1, Opcode.ALOAD_2);
	}

	private InstructionSequence buildSequence(Opcode... opcodes)
	{
		return new InstructionSequence(Arrays.asList(opcodes));
	}

	private void checkSequence(Map<InstructionSequence, Integer> scoreMap, int count, Opcode... opcodes)
	{
		InstructionSequence key = buildSequence(opcodes);

		int score = scoreMap.get(key);

		assertEquals(count, score);
	}

	private void log(Map<InstructionSequence, Integer> result)
	{
		if (SHOW_OUTPUT)
		{
			for (Map.Entry<InstructionSequence, Integer> entry : result.entrySet())
			{
				System.out.println(entry.getValue() + "\t=>\t" + entry.getKey());
			}
		}
	}

	@Test
	public void testInfiniteLoopChain1()
	{
		String[] lines = new String[] {
				"0: aload_1",
				"1: invokevirtual #38                 // Method org/adoptopenjdk/jitwatch/model/Tag.getName:()Ljava/lang/String;",
				"4: astore_3",
				"5: iconst_m1",
				"6: istore        4",
				"8: goto          0"
		};

		MemberBytecode memberBytecode = createMemberBytecode(lines);

		SequenceCountOperation counter = new SequenceCountOperation(1);

		counter.processInstructions("Foo", memberBytecode);

		Map<InstructionSequence, Integer> result = counter.getSequenceScores();

		log(result);

		assertEquals(6, result.size());

		checkSequence(result, 1, Opcode.ALOAD_1);
		checkSequence(result, 1, Opcode.INVOKEVIRTUAL);
		checkSequence(result, 1, Opcode.ASTORE_3);
		checkSequence(result, 1, Opcode.ICONST_M1);
		checkSequence(result, 1, Opcode.ISTORE);
		checkSequence(result, 1, Opcode.GOTO);
	}

	@Test
	public void testInfiniteLoopChain2()
	{
		String[] lines = new String[] {
				"0: aload_1",
				"1: invokevirtual #38                 // Method org/adoptopenjdk/jitwatch/model/Tag.getName:()Ljava/lang/String;",
				"4: astore_3",
				"5: iconst_m1",
				"6: istore        4",
				"8: goto          0"
		};

		MemberBytecode memberBytecode = createMemberBytecode(lines);

		SequenceCountOperation counter = new SequenceCountOperation(2);

		counter.processInstructions("Foo", memberBytecode);

		Map<InstructionSequence, Integer> result = counter.getSequenceScores();

		log(result);

		assertEquals(6, result.size());

		checkSequence(result, 1, Opcode.ALOAD_1, Opcode.INVOKEVIRTUAL);
		checkSequence(result, 1, Opcode.INVOKEVIRTUAL, Opcode.ASTORE_3);
		checkSequence(result, 1, Opcode.ASTORE_3, Opcode.ICONST_M1);
		checkSequence(result, 1, Opcode.ICONST_M1, Opcode.ISTORE);
		checkSequence(result, 1, Opcode.ISTORE, Opcode.GOTO);
		checkSequence(result, 1, Opcode.GOTO, Opcode.ALOAD_1);
	}

	@Test
	public void testInfiniteLoopChain3()
	{
		String[] lines = new String[] {
				"0: aload_1",
				"1: invokevirtual #38                 // Method org/adoptopenjdk/jitwatch/model/Tag.getName:()Ljava/lang/String;",
				"4: astore_3",
				"5: iconst_m1",
				"6: istore        4",
				"8: goto          0"
		};

		MemberBytecode memberBytecode = createMemberBytecode(lines);

		SequenceCountOperation counter = new SequenceCountOperation(3);

		counter.processInstructions("Foo", memberBytecode);

		Map<InstructionSequence, Integer> result = counter.getSequenceScores();

		log(result);

		assertEquals(6, result.size());

		checkSequence(result, 1, Opcode.ALOAD_1, Opcode.INVOKEVIRTUAL, Opcode.ASTORE_3);
		checkSequence(result, 1, Opcode.INVOKEVIRTUAL, Opcode.ASTORE_3, Opcode.ICONST_M1);
		checkSequence(result, 1, Opcode.ASTORE_3, Opcode.ICONST_M1, Opcode.ISTORE);
		checkSequence(result, 1, Opcode.ICONST_M1, Opcode.ISTORE, Opcode.GOTO);
		checkSequence(result, 1, Opcode.ISTORE, Opcode.GOTO, Opcode.ALOAD_1);
		checkSequence(result, 1, Opcode.GOTO, Opcode.ALOAD_1, Opcode.INVOKEVIRTUAL);

	}

	@Test
	public void testFollowGotoChain1()
	{
		String[] lines = new String[] {
				"0: goto          2",
				"1: goto          3",
				"2: goto          1",
				"3: return        "
		};

		MemberBytecode memberBytecode = createMemberBytecode(lines);

		SequenceCountOperation counter = new SequenceCountOperation(1);

		counter.processInstructions("Foo", memberBytecode);

		Map<InstructionSequence, Integer> result = counter.getSequenceScores();

		log(result);

		assertEquals(2, result.size());

		checkSequence(result, 3, Opcode.GOTO);
		checkSequence(result, 1, Opcode.RETURN);
	}

	@Test
	public void testFollowGotoChain2()
	{
		String[] lines = new String[] {
				"0: goto          2",
				"1: goto          3",
				"2: goto          1",
				"3: return        "
		};

		MemberBytecode memberBytecode = createMemberBytecode(lines);

		SequenceCountOperation counter = new SequenceCountOperation(2);

		counter.processInstructions("Foo", memberBytecode);

		Map<InstructionSequence, Integer> result = counter.getSequenceScores();

		log(result);

		assertEquals(2, result.size());

		checkSequence(result, 2, Opcode.GOTO, Opcode.GOTO);
		checkSequence(result, 1, Opcode.GOTO, Opcode.RETURN);
	}

	@Test
	public void testAthrow()
	{
		String[] lines = new String[] {
				"0: getstatic       #5   // Field socketImplCtor:Ljava/lang/reflect/Constructor;",
				"3: iconst_0        ",
				"4: anewarray       #6   // class java/lang/Object",
				"7: invokevirtual   #7   // Method java/lang/reflect/Constructor.newInstance:([Ljava/lang/Object;)Ljava/lang/Object;",
				"10: checkcast       #8   // class java/net/SocketImpl",
				"13: areturn         ",
				"14: astore_0        ",
				"15: new             #10  // class java/lang/AssertionError",
				"18: dup             ",
				"19: aload_0         ",
				"20: invokespecial   #11  // Method java/lang/AssertionError.\"<init>\":(Ljava/lang/Object;)V",
				"23: athrow          ",
				"24: astore_0        ",
				"25: new             #10  // class java/lang/AssertionError",
				"28: dup             ",
				"29: aload_0         ",
				"30: invokespecial   #11  // Method java/lang/AssertionError.\"<init>\":(Ljava/lang/Object;)V",
				"33: athrow          ",
				"34: astore_0        ",
				"35: new             #10  // class java/lang/AssertionError",
				"38: dup             ",
				"39: aload_0         ",
				"40: invokespecial   #11  // Method java/lang/AssertionError.\"<init>\":(Ljava/lang/Object;)V",
				"43: athrow        "
		};

		MemberBytecode memberBytecode = createMemberBytecode(lines);

		SequenceCountOperation counter = new SequenceCountOperation(3);

		counter.processInstructions("Foo", memberBytecode);

		Map<InstructionSequence, Integer> result = counter.getSequenceScores();

		log(result);

		assertEquals(8, result.size());

		checkSequence(result, 1, Opcode.GETSTATIC, Opcode.ICONST_0, Opcode.ANEWARRAY);
		checkSequence(result, 1, Opcode.ICONST_0, Opcode.ANEWARRAY, Opcode.INVOKEVIRTUAL);
		checkSequence(result, 1, Opcode.ANEWARRAY, Opcode.INVOKEVIRTUAL, Opcode.CHECKCAST);
		checkSequence(result, 1, Opcode.INVOKEVIRTUAL, Opcode.CHECKCAST, Opcode.ARETURN);

		checkSequence(result, 3, Opcode.ASTORE_0, Opcode.NEW, Opcode.DUP);
		checkSequence(result, 3, Opcode.NEW, Opcode.DUP, Opcode.ALOAD_0);
		checkSequence(result, 3, Opcode.DUP, Opcode.ALOAD_0, Opcode.INVOKESPECIAL);
		checkSequence(result, 3, Opcode.ALOAD_0, Opcode.INVOKESPECIAL, Opcode.ATHROW);
	}
}