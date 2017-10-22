package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;

import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.junit.Test;

public class TestMemberBytecode
{
	@Test
	public void testSingleBackBranch()
	{
		String[] lines = new String[] {
				"0: iconst_0",
				"1: istore_1",
				"2: iload_1",
				"3: bipush        100",
				"5: if_icmpge     30",
				"8: iload_1",
				"9: iconst_2",
				"10: irem",
				"11: ifne          17",
				"14: goto          24",
				"17: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;",
				"20: iload_1",
				"21: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V",
				"24: iinc          1, 1",
				"27: goto          2",
				"30: return" };

		MemberBytecode memberBytecode = UnitTestUtil.createMemberBytecode(lines);

		int lastBackBranchBCI = memberBytecode.findLastBackBranchToBCI(2);

		assertEquals(27, lastBackBranchBCI);

	}

	@Test
	public void testMultipleBackBranches()
	{
		String[] lines = new String[] {
				"0: iconst_0",
				"1: istore_1",
				"2: iload_1",
				"3: bipush        100",
				"5: if_icmpge     21",
				"8: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;",
				"11: iload_1",
				"12: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V",
				"15: iinc          1, 1",
				"18: goto          2",
				"21: iconst_0",
				"22: istore_1",
				"23: iload_1",
				"24: bipush        100",
				"26: if_icmpge     42",
				"29: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;",
				"32: iload_1",
				"33: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V",
				"36: iinc          1, 1",
				"39: goto          23",
				"42: iconst_0",
				"43: istore_1",
				"44: iload_1",
				"45: bipush        100",
				"47: if_icmpge     63",
				"50: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;",
				"53: iload_1",
				"54: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V",
				"57: iinc          1, 1",
				"60: goto          44",
				"63: iconst_0",
				"64: istore_1",
				"65: iload_1",
				"66: bipush        100",
				"68: if_icmpge     84",
				"71: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;",
				"74: iload_1",
				"75: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V",
				"78: iinc          1, 1",
				"81: goto          65",
				"84: return" };

		MemberBytecode memberBytecode = UnitTestUtil.createMemberBytecode(lines);

		assertEquals(18, memberBytecode.findLastBackBranchToBCI(2));
		assertEquals(39, memberBytecode.findLastBackBranchToBCI(23));
		assertEquals(60, memberBytecode.findLastBackBranchToBCI(44));
		assertEquals(81, memberBytecode.findLastBackBranchToBCI(65));
	}
}
