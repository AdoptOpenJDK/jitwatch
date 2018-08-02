/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.parser;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.parser.hotspot.HotSpotLogParser;
import org.adoptopenjdk.jitwatch.parser.j9.J9LogParser;
import org.adoptopenjdk.jitwatch.parser.zing.ZingLogParser;

public class ParserFactory
{
	private ParserFactory()
	{
	}

	public static ILogParser getParser(ParserType parserType, IJITListener jitListener)
	{
		switch (parserType)
		{
		case HOTSPOT:
			return new HotSpotLogParser(jitListener);
		case J9:
			return new J9LogParser(jitListener);
		case ZING:
			return new ZingLogParser(jitListener);
		default:
			throw new RuntimeException("Unknown parser " + parserType);
		}
	}
}