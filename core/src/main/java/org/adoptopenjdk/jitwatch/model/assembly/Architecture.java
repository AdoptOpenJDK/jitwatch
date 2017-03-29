/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

import org.adoptopenjdk.jitwatch.util.StringUtil;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_APOSTROPHE;

public enum Architecture
{
	X86_32, X86_64, ARM_32, ARM_64;

	private static final String ARCH_X86_32 = "i386";
	private static final String ARCH_X86_64 = "i386:x86-64";
	private static final String ARCH_AMD_64 = "amd64";

	private static final String ARCH_ARM_32 = "arm";
	private static final String ARCH_ARM_64 = "arm-64";

	public static Architecture parseFromLogLine(String line)
	{
		String arch = StringUtil.getSubstringBetween(line, S_APOSTROPHE, S_APOSTROPHE);

		if (arch != null)
		{
			switch (arch)
			{
			case ARCH_X86_32:
				return X86_32;
			case ARCH_X86_64:
			case ARCH_AMD_64:
				return X86_64;
			case ARCH_ARM_32:
				return ARM_32;
			case ARCH_ARM_64:
				return ARM_64;
			}
		}

		return null;
	}
}