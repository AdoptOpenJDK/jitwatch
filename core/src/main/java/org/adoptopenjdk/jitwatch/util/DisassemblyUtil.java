/*
 * Copyright (c) 2013-2017 Chris Newland.
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

	public static Path getDisassemblerFilePath()
	{
		String javaHome = System.getProperty("java.home");

		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("java.home is {}", javaHome);
		}

		Path hsdisPath = Paths.get(javaHome);

		Path hsdisPathJRE = Paths.get(hsdisPath.toString(), "jre", "lib");

		if (hsdisPathJRE.toFile().exists())
		{
			hsdisPath = hsdisPathJRE;

			if (DEBUG_LOGGING_ASSEMBLY)
			{
				logger.debug("jre lib folder found {}", hsdisPathJRE);
			}
		}
		else
		{
			hsdisPath = Paths.get(hsdisPath.toString(), "lib");
		}

		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("looking in {}", hsdisPath);
		}

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
		{
			binaryName = "hsdis-i386";
			hsdisPath = Paths.get(hsdisPath.toString(), "i386", "server");
			break;
		}
		case X86_64:
		{
			binaryName = "hsdis-amd64";

			Path hsdisPathAMD64 = Paths.get(hsdisPath.toString(), "amd64");

			if (hsdisPathAMD64.toFile().exists())
			{
				hsdisPath = hsdisPathAMD64;
			}

			Path hsdisPathServer = Paths.get(hsdisPath.toString(), "server");

			if (hsdisPathServer.toFile().exists())
			{
				hsdisPath = hsdisPathServer;
			}

			break;
		}
		case ARM_32:
		{
			binaryName = "hsdis-arm";
			hsdisPath = Paths.get(hsdisPath.toString(), "arm", "server");
			break;

		}
		case ARM_64:
		{
			binaryName = "hsdis-arm";
			hsdisPath = Paths.get(hsdisPath.toString(), "arm64", "server"); // TODO
																			// untested
			break;
		}
		default:
			break;
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
}