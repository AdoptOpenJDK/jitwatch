/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DisassemblyUtil
{
	private static final Logger logger = LoggerFactory.getLogger(DisassemblyUtil.class);

	enum OperatingSystem
	{
		WIN, MAC, LINUX
	}

	enum Architecture
	{
		BIT32, BIT64
	}

	private DisassemblyUtil()
	{
	}

	public static boolean isDisassemblerAvailable()
	{
		boolean found = false;

		String jrePath = System.getProperty("java.home");

		File jreDir = new File(jrePath);

		File hsdisFile = null;

		if (jreDir.exists() && jreDir.isDirectory())
		{
			String binaryPath = getDisassemblerFilePath();

			hsdisFile = new File(jreDir, binaryPath);

			logger.debug("looking for hsdis binary: {}", hsdisFile);

			if (hsdisFile.exists() && hsdisFile.isFile())
			{
				found = true;
			}
		}

		return found;
	}

	public static String getDisassemblerFilePath()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("lib");

		OperatingSystem os = getOperatingSystem();
		Architecture arch = getArchitecture();

		switch (arch)
		{
		case BIT32:
			builder.append(File.separator);
			builder.append("i386");
			builder.append(File.separator);
			builder.append("server");
			builder.append(File.separator);
			builder.append("hsdis-i386");
			break;
		case BIT64:
			if (os != null && !os.equals(OperatingSystem.MAC))
			{
				builder.append(File.separator);
				builder.append("amd64");
			}
			builder.append(File.separator);
			builder.append("server");
			builder.append(File.separator);
			builder.append("hsdis-amd64");
			break;
		}

		if (os != null)
		{
			switch (os)
			{
			case WIN:
				builder.append(".dll");
				break;
			case MAC:
				builder.append(".dylib");
				break;
			case LINUX:
				builder.append(".so");
				break;
			}
		}

		return builder.toString();
	}

	private static OperatingSystem getOperatingSystem()
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

	private static Architecture getArchitecture()
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

    public static String getGcc()
   	{
   		switch (getArchitecture()) {
   		   	case BIT64:
   				return "mingw64-x86_64-gcc-core";
   			case BIT32:
   				return "mingw64-i686-gcc-core";
   			default:
   				throw new IllegalStateException("Unknown architecture");
   		}
   	}

    public static String getMingw()
   	{
   		switch (getArchitecture()) {
   		   	case BIT64:
   				return "x86_64-w64-mingw32";
   			case BIT32:
   				return "i686-w64-mingw32";
   			default:
   				throw new IllegalStateException("Unknown architecture");
   		}
   	}
}
