/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.parser.j9.J9Line;
import org.adoptopenjdk.jitwatch.parser.j9.J9Util;
import org.junit.Test;

public class TestJ9Parser
{
	@Test
	public void testParseLine() throws Exception
	{
		String line = "+ (cold) java/lang/Double.longBitsToDouble(J)D @ 00007F0AAA60003C-00007F0AAA60005E OrdinaryMethod Q_SZ=0 Q_SZI=0 QW=1 j9m=0000000001E31FE0 bcsz=3 JNI compThread=0 CpuLoad=8%(4%avg) JvmCpu=0%";

		J9Line j9Line = J9Util.parseLine(line);
		
		assertEquals(J9Line.TEMPERATURE_COLD, j9Line.getTemperature());

		assertEquals("java/lang/Double.longBitsToDouble(J)D", j9Line.getSignature());
		
		assertEquals("java/lang/Double longBitsToDouble (J)D", J9Util.convertJ9SigToLogCompilationSignature(j9Line.getSignature()));

		MemberSignatureParts msp = j9Line.getMemberSignatureParts();

		assertEquals("java.lang.Double", msp.getFullyQualifiedClassName());
		assertEquals("longBitsToDouble", msp.getMemberName());
		assertEquals("double", msp.getReturnType());
		assertEquals(1, msp.getParamTypes().size());
		assertEquals("long", msp.getParamTypes().get(0));

		assertEquals("00007F0AAA60003C", j9Line.getRangeStart());
		assertEquals("00007F0AAA60005E", j9Line.getRangeEnd());
		
		assertEquals(0x22, j9Line.getNativeSize());

		assertTrue(j9Line.hasFeature("OrdinaryMethod"));

		assertEquals(3, j9Line.getBytecodeSize());		
	}
	
	@Test
	public void testParseLineWiothObjectReturn() throws Exception
	{
		String line = "+ (cold) java/lang/System.getEncoding(I)Ljava/lang/String; @ 00007F0AAA60007C-00007F0AAA6001C9 OrdinaryMethod Q_SZ=0 Q_SZI=0 QW=1 j9m=0000000001E1F440 bcsz=3 JNI compThread=0 CpuLoad=8%(4%avg) JvmCpu=0%";

		J9Line j9Line = J9Util.parseLine(line);
		
		assertEquals(J9Line.TEMPERATURE_COLD, j9Line.getTemperature());

		assertEquals("java/lang/System.getEncoding(I)Ljava/lang/String;", j9Line.getSignature());
		
		assertEquals("java/lang/System getEncoding (I)Ljava/lang/String;", J9Util.convertJ9SigToLogCompilationSignature(j9Line.getSignature()));

		MemberSignatureParts msp = j9Line.getMemberSignatureParts();

		assertEquals("java.lang.System", msp.getFullyQualifiedClassName());
		assertEquals("getEncoding", msp.getMemberName());
		assertEquals("java.lang.String", msp.getReturnType());
		assertEquals(1, msp.getParamTypes().size());
		assertEquals("int", msp.getParamTypes().get(0));

		assertEquals("00007F0AAA60007C", j9Line.getRangeStart());
		assertEquals("00007F0AAA6001C9", j9Line.getRangeEnd());
		
		assertEquals(0x14D, j9Line.getNativeSize());

		assertTrue(j9Line.hasFeature("OrdinaryMethod"));

		assertEquals(3, j9Line.getBytecodeSize());		
	}
	
	@Test
	public void testParseLineWithComplexTemperature() throws Exception
	{
		String line = "+ (profiled very-hot) java/util/ComparableTimSort.mergeLo(IIII)V @ 00007F0AAA62BBA0-00007F0AAA633020 OrdinaryMethod 16.40% T Q_SZ=0 Q_SZI=0 QW=100 j9m=000000000204A130 bcsz=656 compThread=0 CpuLoad=100%(50%avg) JvmCpu=100%";

		J9Line j9Line = J9Util.parseLine(line);

		assertEquals(J9Line.TEMPERATURE_PROFILED_VERY_HOT, j9Line.getTemperature());

		assertEquals("java/util/ComparableTimSort.mergeLo(IIII)V", j9Line.getSignature());

		assertEquals("java/util/ComparableTimSort mergeLo (IIII)V", J9Util.convertJ9SigToLogCompilationSignature(j9Line.getSignature()));

		MemberSignatureParts msp = j9Line.getMemberSignatureParts();

		assertEquals("java.util.ComparableTimSort", msp.getFullyQualifiedClassName());
		assertEquals("mergeLo", msp.getMemberName());
		assertEquals("void", msp.getReturnType());
		assertEquals(4, msp.getParamTypes().size());
		assertEquals("int", msp.getParamTypes().get(0));
		assertEquals("int", msp.getParamTypes().get(1));
		assertEquals("int", msp.getParamTypes().get(2));
		assertEquals("int", msp.getParamTypes().get(3));

		assertEquals("00007F0AAA62BBA0", j9Line.getRangeStart());
		assertEquals("00007F0AAA633020", j9Line.getRangeEnd());

		assertTrue(j9Line.hasFeature("OrdinaryMethod"));

		assertEquals(656, j9Line.getBytecodeSize());
	}
}