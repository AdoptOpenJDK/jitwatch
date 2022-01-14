/*
 * Copyright (c) 2013-2022 Chris Newland.
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
import com.chrisnewland.freelogj.Logger;
import com.chrisnewland.freelogj.LoggerFactory;

public final class DisassemblyUtil
{
	private static final Logger logger = LoggerFactory.getLogger(DisassemblyUtil.class);

	private DisassemblyUtil()
	{
	}

	public static boolean isDisassemblerAvailable()
	{
		boolean found = downloadedDisassemblerPresent();

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

	public static String getDynamicLibraryPath()
	{
		switch (OSUtil.getOperatingSystem())
		{
		case WIN:
			return "PATH";
		case MAC:
			return "DYLD_LIBRARY_PATH";
		case LINUX:
			return "LD_LIBRARY_PATH";
		}

		throw new RuntimeException("Unknown OS");
	}

	public static String getDisassemblerFilename()
	{
		OperatingSystem os = OSUtil.getOperatingSystem();
		Architecture arch = OSUtil.getArchitecture();

		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("OS: {} Arch: {}", os, arch);
		}

		String binaryName = "hsdis-" + System.getProperty("os.arch");

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

	public static boolean downloadedDisassemblerPresent()
	{
		return new File(getDisassemblerFilename()).exists();
	}

	public static Path getDisassemblerFilePath()
	{
		String binaryName = getDisassemblerFilename();

		// first check the dynamic library path in case user has overridden JDK location for hsdis
		String dynLibPath = System.getenv(getDynamicLibraryPath());

		if (dynLibPath != null)
		{
			String[] dirs = dynLibPath.split(":");

			for (String dir : dirs)
			{
				Path path = Paths.get(dir, binaryName);

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

		// next search JDK
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

		String[] libPath = new String[] { "lib", "bin", "" };

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

		// finally search for locally downloaded
		if (downloadedDisassemblerPresent())
		{
			return Paths.get(getDisassemblerFilename());
		}

		return null;
	}
}