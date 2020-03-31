/*
 * Copyright (c) 2013-2020 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_ASSEMBLY;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.adoptopenjdk.jitwatch.model.assembly.Architecture;
import org.adoptopenjdk.jitwatch.util.OSUtil.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DisassemblyUtil
{
	private static final Logger logger = LoggerFactory.getLogger(DisassemblyUtil.class);

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

			if (DEBUG_LOGGING_ASSEMBLY)
			{
				logger.debug("looking for hsdis binary: {}", hsdisFile);
			}

			if (hsdisFile.exists() && hsdisFile.isFile())
			{
				found = true;
			}
		}

		return found;
	}

	private static String getDisassemblerFilename()
	{
		OperatingSystem os = OSUtil.getOperatingSystem();
		Architecture arch = OSUtil.getArchitecture();

		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("OS: {} Arch: {}", os, arch);
		}

		String binaryName = null;

		switch (arch)
		{
		case X86_32:
			binaryName = "hsdis-i386";
			break;

		case X86_64:
			binaryName = "hsdis-amd64";
			break;

		case ARM_32:
			binaryName = "hsdis-arm";
			break;

		case ARM_64:
			binaryName = "hsdis-arm";
			break;

		default:
			break;
		}

		if (os != null)
		{
			switch (os)
			{
			case WIN:
				binaryName += ".dll";
				break;
			case MAC:
				binaryName += ".dylib";
				break;
			case LINUX:
				binaryName += ".so";
				break;
			}
		}

		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("binaryName: {}", binaryName);
		}

		return binaryName;
	}

	public static Path getDisassemblerFilePath()
	{
		String binaryName = getDisassemblerFilename();

		String javaHome = System.getProperty("java.home", "");

		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("java.home is {}", javaHome);
		}

		if (javaHome.endsWith("jre"))
		{
			javaHome = javaHome.substring(0, javaHome.length() - 3);
		}

		String[] jrePath = new String[] { "jre", "" };

		String[] libPath = new String[] { "lib", "" };

		String[] serverPath = new String[] { "server", "" };

		String[] archPath = new String[] { "i386", "amd64", "" };

		for (String jre : jrePath)
		{
			for (String lib : libPath)
			{
				for (String server : serverPath)
				{
					for (String arch : archPath)
					{
						Path path = Paths.get(javaHome, jre, lib, server, arch, binaryName);

						if (DEBUG_LOGGING_ASSEMBLY)
						{
							logger.debug("looking in {}", path);
						}

						File file = path.toFile();

						if (file.exists() && file.isFile())
						{
							return path;
						}
					}
				}
			}
		}

		return null;
	}
}