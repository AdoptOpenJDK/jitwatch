/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.parser;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.parser.hotspot.HotSpotLogParser;
import org.adoptopenjdk.jitwatch.parser.j9.J9LogParser;

public class ParserFactory
{
	public static final String PARSER_HOTSPOT = "hotspot";
	
	public static final String PARSER_J9 = "j9";

	private ParserFactory()
	{
	}

	public static ILogParser getParser(IJITListener jitListener)
	{
		String parserProperty = System.getProperty("jitwatch.parser", PARSER_HOTSPOT);
		
		switch (parserProperty)
		{
		case PARSER_HOTSPOT:
			return new HotSpotLogParser(jitListener);
		case PARSER_J9:
			return new J9LogParser(jitListener);
			default:
				throw new RuntimeException("Unknown parser " + parserProperty);
		}
	}
}