package com.chrisnewland.jitwatch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DisassemblyUtil
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

    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private DisassemblyUtil() {
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

			logger.info("looking for hsdis binary: {}", hsdisFile);

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
			builder.append(File.separator);
			builder.append("amd64");
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
}
