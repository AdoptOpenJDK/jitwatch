/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OSUtil
{
	private static final Logger logger = LoggerFactory.getLogger(OSUtil.class);

	public enum OperatingSystem
	{
		WIN, MAC, LINUX
	}

	public enum Architecture
	{
		BIT32, BIT64
	}
	
	public static OperatingSystem getOperatingSystem()
	{
		String osNameProperty = System.getProperty("os.name");

		if (osNameProperty != null)
		{
			osNameProperty = osNameProperty.toLowerCase();

			if (osNameProperty.contains("win"))
			{
				return OperatingSystem.WIN;
			}
			else if (osNameProperty.contains("mac"))
			{
				return OperatingSystem.MAC;
			}
			else if (osNameProperty.contains("linux") || osNameProperty.contains("nix"))
			{
				return OperatingSystem.LINUX;
			}
		}

		logger.error("Unknown OS name: {}", osNameProperty);
		
		return null;
	}

	public static Architecture getArchitecture()
	{
		String osArch = System.getProperty("os.arch");

		if (osArch != null && osArch.contains("64"))
		{
			return Architecture.BIT64;
		}
		else
		{
			return Architecture.BIT32;
		}
	}
}
