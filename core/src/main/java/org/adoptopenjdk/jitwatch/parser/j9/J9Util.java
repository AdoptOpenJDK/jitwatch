/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.parser.j9;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;

public class J9Util
{
	public static J9Line parseLine(String line)
	{
		J9Line result = new J9Line();

		int closeParentheses = line.indexOf(')');

		if (closeParentheses != -1)
		{
			int openParentheses = line.indexOf('(');

			if (openParentheses != -1 && openParentheses < closeParentheses)
			{
				String temperature = line.substring(openParentheses + 1, closeParentheses);

				result.setTemperature(temperature);

				line = line.substring(closeParentheses + 1).trim();
			}
		}

		String[] parts = line.split(" ");
		
		int pos = 0;

		String signature = parts[pos++];

		pos++; // @ sign

		String range = parts[pos++];

		result.setSignature(signature);
		result.setRange(range);

		for (int i = pos; i < parts.length; i++)
		{
			if (parts[i].contains("="))
			{
				String[] kvParts = parts[i].split("=");

				if (kvParts.length == 2)
				{
					result.addAttribute(kvParts[0], kvParts[1]);
				}
			}
			else
			{
				result.addFeatures(parts[i]);
			}
		}

		return result;
	}

	public static String convertJ9SigToLogCompilationSignature(String j9Signature)
	{
		return j9Signature.replace(C_DOT, C_SPACE).replace("(", " (");
	}
}