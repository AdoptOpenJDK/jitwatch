/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.parser.zing.ZingLine;
import org.adoptopenjdk.jitwatch.parser.zing.ZingLogParser;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.junit.Test;

public class TestZingParser
{
	private ZingLogParser getParser()
	{
		return new ZingLogParser(new IJITListener()
		{

			@Override
			public void handleLogEntry(String entry)
			{
			}

			@Override
			public void handleErrorEntry(String entry)
			{
			}

			@Override
			public void handleReadStart()
			{
			}

			@Override
			public void handleReadComplete()
			{
			}

			@Override
			public void handleJITEvent(JITEvent event)
			{
			}
		});
	}

	@Test
	public void testParseLineQueued() throws Exception
	{
		String lineQueued = "0.426: 930 3 java.lang.String::indexOf (II)I (70 score) (71 bytes) 0.425883";

		ZingLogParser parser = getParser();

		ZingLine zingLine = parser.parseLine(lineQueued);

		assertEquals(426, zingLine.getTimestampMillisCompileStart());
		assertEquals(930, zingLine.getCompileId());
		assertEquals(3, zingLine.getTier());
		assertEquals("java/lang/String indexOf (II)I", zingLine.getSignature());
		assertEquals(70, zingLine.getScore());
		assertEquals(71, zingLine.getBytecodeSize());
	}

	@Test
	public void testParseLineInstalledWithStashedCompile() throws Exception
	{
		String lineQueued = "0.426: 930 3 java.lang.String::indexOf (II)I (70 score) (71 bytes) 0.425883";

		ZingLogParser parser = getParser();

		parser.parseLine(lineQueued);

		String lineInstalled = "0.429: 930 3 installed at 0x30014450 with size 0x117 from object cache ( java.lang.String::indexOf waited 0 ms, compile time 2 / 2 ms ) 0.428904";

		ZingLine zingLine = parser.parseLine(lineInstalled);

		assertEquals(426, zingLine.getTimestampMillisCompileStart());
		assertEquals(429, zingLine.getTimestampMillisNMethodEmitted());
		assertEquals(930, zingLine.getCompileId());
		assertEquals(3, zingLine.getTier());
		assertEquals("java/lang/String indexOf (II)I", zingLine.getSignature());
		assertEquals(ParseUtil.parseHexAddress("0x30014450"), zingLine.getStartAddress());
		assertEquals(ParseUtil.parseHexAddress("0x30014450") + ParseUtil.parseHexAddress("0x117"), zingLine.getEndAddress());
		assertEquals(ParseUtil.parseHexAddress("0x117"), zingLine.getNativeSize());
		assertTrue(zingLine.isStashedCompile());
		assertEquals(70, zingLine.getScore());
		assertEquals(71, zingLine.getBytecodeSize());
	}

	@Test
	public void testParseLineInstalledWithoutStashedCompile() throws Exception
	{
		String lineQueued = "0.777: 1098 1 sun.misc.FloatingDecimal::appendTo (DLjava/lang/Appendable;)V (11 score) (11 bytes) 0.776559";

		ZingLogParser parser = getParser();

		parser.parseLine(lineQueued);

		String lineInstalled = "0.777: 1098 1 installed at 0x31b36240 with size 0xc4 ( sun.misc.FloatingDecimal::appendTo waited 0 ms, compile time 0 / 0 ms ) 0.776716";

		ZingLine zingLine = parser.parseLine(lineInstalled);

		assertEquals(777, zingLine.getTimestampMillisCompileStart());
		assertEquals(777, zingLine.getTimestampMillisNMethodEmitted());
		assertEquals(1098, zingLine.getCompileId());
		assertEquals(1, zingLine.getTier());
		assertEquals("sun/misc/FloatingDecimal appendTo (DLjava/lang/Appendable;)V", zingLine.getSignature());
		assertEquals(ParseUtil.parseHexAddress("0x31b36240"), zingLine.getStartAddress());
		assertEquals(ParseUtil.parseHexAddress("0x31b36240") + ParseUtil.parseHexAddress("0xc4"), zingLine.getEndAddress());
		assertEquals(ParseUtil.parseHexAddress("0xc4"), zingLine.getNativeSize());
		assertTrue(!zingLine.isStashedCompile());
		assertTrue(!zingLine.isThrowsExceptions());
	}

	@Test
	public void testParseLineQueuedWithException() throws Exception
	{
		String line = "0.584: 1071   !   1 java.nio.charset.CharsetEncoder::encode (Ljava/nio/CharBuffer;Ljava/nio/ByteBuffer;Z)Ljava/nio/charset/CoderResult; (285 score) (285 bytes) 0.584284";

		ZingLogParser parser = getParser();

		ZingLine zingLine = parser.parseLine(line);

		assertEquals(584, zingLine.getTimestampMillisCompileStart());
		assertEquals(1071, zingLine.getCompileId());
		assertTrue(zingLine.isThrowsExceptions());
		assertEquals(1, zingLine.getTier());
		assertEquals(
				"java/nio/charset/CharsetEncoder encode (Ljava/nio/CharBuffer;Ljava/nio/ByteBuffer;Z)Ljava/nio/charset/CoderResult;",
				zingLine.getSignature());
		assertEquals(285, zingLine.getScore());
		assertEquals(285, zingLine.getBytecodeSize());
	}

	@Test
	public void testParseLineQueuedWithMultipleFlags() throws Exception
	{
		String line = "0.584: 1071   s%!   1 java.nio.charset.CharsetEncoder::encode (Ljava/nio/CharBuffer;Ljava/nio/ByteBuffer;Z)Ljava/nio/charset/CoderResult; (285 score) (285 bytes) 0.584284";

		ZingLogParser parser = getParser();

		ZingLine zingLine = parser.parseLine(line);

		assertEquals(584, zingLine.getTimestampMillisCompileStart());
		assertEquals(1071, zingLine.getCompileId());
		assertTrue(zingLine.isThrowsExceptions());
		assertEquals(1, zingLine.getTier());
		assertEquals(
				"java/nio/charset/CharsetEncoder encode (Ljava/nio/CharBuffer;Ljava/nio/ByteBuffer;Z)Ljava/nio/charset/CoderResult;",
				zingLine.getSignature());
		assertEquals(285, zingLine.getScore());
		assertEquals(285, zingLine.getBytecodeSize());
	}

	@Test
	public void testLineContainingBCI() throws Exception
	{
		String line = "0.720: 1087 %     3 Hello::main ([Ljava/lang/String;)V @ 19 (69 score) (69 bytes) 0.720313";

		ZingLogParser parser = getParser();

		ZingLine zingLine = parser.parseLine(line);

		assertEquals(720, zingLine.getTimestampMillisCompileStart());
		assertEquals(1087, zingLine.getCompileId());
		assertTrue(!zingLine.isThrowsExceptions());
		assertEquals(3, zingLine.getTier());
		assertEquals("Hello main ([Ljava/lang/String;)V", zingLine.getSignature());
		assertEquals(69, zingLine.getScore());
		assertEquals(69, zingLine.getBytecodeSize());
	}

	@Test
	public void testWaitedTime()
	{
		String lineInstalled = "0.777: 1098 1 installed at 0x31b36240 with size 0xc4 ( sun.misc.FloatingDecimal::appendTo waited 123 ms, compile time 0 / 0 ms ) 0.776716";

		ZingLogParser parser = getParser();

		assertEquals(123, parser.getWaitedTime(lineInstalled));
	}

	@Test
	public void testCompileTime()
	{
		String lineInstalled = "0.777: 1098 1 installed at 0x31b36240 with size 0xc4 ( sun.misc.FloatingDecimal::appendTo waited 123 ms, compile time 456 / 456 ms ) 0.776716";

		ZingLogParser parser = getParser();

		assertEquals(456, parser.getCompileTime(lineInstalled));
	}

	@Test
	public void testTiming()
	{
		String lineQ = "0.375:  162       1 sun.nio.cs.StandardCharsets$Aliases::init ([Ljava/lang/Object;)V (3945 score) (3945 bytes) 0.375037";

		String lineI = "0.404:  162       1 installed at 0x31abde40 with size 0x19a6f  ( sun.nio.cs.StandardCharsets$Aliases::init waited 254 ms, compile time 29 / 29 ms ) 0.404263";

		ZingLogParser parser = getParser();

		parser.parseLine(lineQ);
		ZingLine zingLineI = parser.parseLine(lineI);

		assertEquals(375 - 254, zingLineI.getTimestampMillisQueued());
		assertEquals(375, zingLineI.getTimestampMillisCompileStart());
		assertEquals(404, zingLineI.getTimestampMillisNMethodEmitted());
	}

	@Test
	public void testActualLogLines()
	{
		ZingLogParser parser = getParser();

		assertEquals(0,
				parser
						.getLogLineIndex(
								"0.720: 1087 %     3 Hello::main ([Ljava/lang/String;)V @ 19 (69 score) (69 bytes) 0.720313"));
		assertEquals(-1, parser.getLogLineIndex("Total wait in queue time (s):       	0.0       -0.0      0.0"));
		assertEquals(11,
				parser
						.getLogLineIndex(
								"Hello Zing 0.590: 1079       1 java.io.OutputStream::flush ()V (1 score) (1 bytes) 0.589814"));
	}
}