/*
 * Copyright (c) 2013-2020 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

import org.adoptopenjdk.jitwatch.model.assembly.arm.AssemblyParserARM;
import org.adoptopenjdk.jitwatch.model.assembly.x86.AssemblyParserX86;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public final class AssemblyUtil
{
	private static final Logger logger = LoggerFactory.getLogger(AssemblyUtil.class);

	private AssemblyUtil()
	{
	}

	public static long getValueFromAddress(final String address)
	{
		long addressValue = 0;

		if (address != null)
		{
			String trimmedAddress = address.trim();

			if (trimmedAddress.startsWith(S_HEX_PREFIX))
			{
				trimmedAddress = trimmedAddress.substring(S_HEX_PREFIX.length());
			}

			if (trimmedAddress.endsWith(S_HEX_POSTFIX))
			{
				trimmedAddress = trimmedAddress.substring(0, trimmedAddress.length() - 1);
			}

			addressValue = Long.parseLong(trimmedAddress, 16);
		}
		return addressValue;
	}

	public static IAssemblyParser getParserForArchitecture(Architecture architecture)
	{
		IAssemblyParser parser = null;

		if (architecture != null)
		{
			switch (architecture)
			{
			case ARM_32:
			case ARM_64:
				parser = new AssemblyParserARM(architecture);
				break;
			case X86_32:
			case X86_64:
				parser = new AssemblyParserX86(architecture);
				break;
			default:
				break;
			}
		}

		if (parser == null)
		{
			logger.debug("Unknown/missing architecture: {}, defaulting to x86 assembly parser", architecture);
			parser = new AssemblyParserX86(architecture);
		}

		return parser;
	}
}