/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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

		Path binaryPath = getDisassemblerFilePath();

		if (binaryPath != null)
		{
			File hsdisFile = binaryPath.toFile();

			logger.debug("looking for hsdis binary: {}", hsdisFile);

			if (hsdisFile.exists() && hsdisFile.isFile())
			{
				found = true;
			}
		}

		return found;
	}

	public static Path getDisassemblerFilePath()
	{
		String javaHome = System.getProperty("java.home");

		Path hsdisPath = Paths.get(javaHome);

		Path hsdisPathJRE = Paths.get(hsdisPath.toString(), "jre", "lib");

		if (hsdisPathJRE.toFile().exists())
		{
			hsdisPath = hsdisPathJRE;
		}
		else
		{
			hsdisPath = Paths.get(hsdisPath.toString(), "lib");
		}

		OperatingSystem os = getOperatingSystem();
		Architecture arch = getArchitecture();

		String binaryName = null;

		switch (arch)
		{
		case BIT32:
		{
			binaryName = "hsdis-i386";
			hsdisPath = Paths.get(hsdisPath.toString(), "i386", "server");
			break;
		}
		case BIT64:
		{
			binaryName = "hsdis-amd64";

			if (os != null && !os.equals(OperatingSystem.MAC))
			{
				hsdisPath = Paths.get(hsdisPath.toString(), "amd64", "server");
			}
			else
			{
				hsdisPath = Paths.get(hsdisPath.toString(), "server");
			}
			break;
		}
		}

		if (os != null)
		{
			switch (os)
			{
			case WIN:
				hsdisPath = Paths.get(hsdisPath.toString(), binaryName + ".dll");
				break;
			case MAC:
				hsdisPath = Paths.get(hsdisPath.toString(), binaryName + ".dylib");
				break;
			case LINUX:
				hsdisPath = Paths.get(hsdisPath.toString(), binaryName + ".so");
				break;
			}
		}

		return hsdisPath;
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
}
