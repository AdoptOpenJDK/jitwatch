/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.loader;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOLLAR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_BACKSLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.adoptopenjdk.jitwatch.jvmlang.LanguageManager;
import org.adoptopenjdk.jitwatch.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceLoader
{
	private static final Logger logger = LoggerFactory.getLogger(ResourceLoader.class);

	private ResourceLoader()
	{
	}

	public static String getSourceForClassName(String fqName, List<String> sourceLocations)
	{
		List<String> searchLocations = new ArrayList<>(sourceLocations);

		if (searchLocations.isEmpty())
		{
			File jdkSrcZip = FileUtil.getJDKSourceZip();

			if (jdkSrcZip != null)
			{
				searchLocations.add(jdkSrcZip.toPath().toString());
			}
		}

		int dollarPos = fqName.indexOf(C_DOLLAR);

		if (dollarPos != -1)
		{
			fqName = fqName.substring(0, dollarPos);
		}

		List<String> fileExtensions = LanguageManager.getKnownFilenameExtensions();

		String result = null;

		for (String suffix : fileExtensions)
		{
			String filename = fqName.replace(S_DOT, File.separator) + S_DOT + suffix;

			result = getSourceForFilename(filename, searchLocations);

			if (result != null)
			{
				break;
			}
		}
		
		return result;
	}

	public static String getSourceForFilename(String fileName, List<String> locations)
	{		
		if (fileName.startsWith(S_SLASH))
		{
			return readFile(new File(fileName));
		}
		
		String source = null;

		for (String location : locations)
		{
			File lf = new File(location);
						
			if (lf.exists())
			{
				if (lf.isDirectory())
				{
					source = readFileInDirectory(lf, fileName);

					if (source != null)
					{
						break;
					}
				}
				else
				{				
					source = readFileFromZip(lf, fileName);

					if (source != null)
					{
						break;
					}
				}
			}
		}

		return source;
	}

	public static String readFileInDirectory(File dir, String fileName)
	{
		File sourceFile = new File(dir, fileName);

		return readFile(sourceFile);
	}

	public static String readFile(File sourceFile)
	{
		String result = null;
		
		if (sourceFile != null && sourceFile.exists())
		{
			try
			{
				byte[] fileBytes = Files.readAllBytes(sourceFile.toPath());
				result = new String(fileBytes, StandardCharsets.UTF_8);
			}
			catch (IOException ioe)
			{
				logger.error("Failed to read file: {}", sourceFile, ioe);
			}
		}

		return result;
	}

	public static Properties readManifestFromZip(File zipFile)
	{
		Properties result = new Properties();

		String manifestSource = readFileFromZip(zipFile, "META-INF/MANIFEST.MF");

		if (manifestSource != null)
		{
			try
			{
				result.load(new StringReader(manifestSource));
			}
			catch (IOException e)
			{
				logger.error("Couldn't read manifest from {}", zipFile, e);
			}
		}
		return result;
	}

	public static String readFileFromZip(File zipFile, String fileName)
	{
		String result = null;

		fileName = fileName.replace(S_BACKSLASH, S_SLASH);
		
		try (ZipFile zf = new ZipFile(zipFile))
		{
			ZipEntry entry = zf.getEntry(fileName);

			if (entry != null)
			{				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(entry))))
				{
					StringBuilder sb = new StringBuilder();

					String line = reader.readLine();

					while (line != null)
					{
						sb.append(line).append(S_NEWLINE);
						line = reader.readLine();
					}

					result = sb.toString();
				}
			}
		}
		catch (IOException ioe)
		{
			logger.error("Could not read file {} from zip {}", fileName, zipFile.getName(), ioe);
		}

		return result;
	}
}
