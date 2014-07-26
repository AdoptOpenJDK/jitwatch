/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.loader;

import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.NEW_LINEFEED;

public final class ResourceLoader
{
	private static final Logger logger = LoggerFactory.getLogger(ResourceLoader.class);

	/*
	 * Hide Utility Class Constructor Utility classes should not have a public
	 * or default constructor.
	 */
	private ResourceLoader()
	{
	}

	public static String getSourceFilename(MetaClass metaClass)
	{
		String fqName = metaClass.getFullyQualifiedName();

		int dollarPos = fqName.indexOf('$');

		if (dollarPos != -1)
		{
			fqName = fqName.substring(0, dollarPos);
		}

		fqName = fqName.replace(S_DOT, File.separator) + ".java";

		return fqName;
	}

	public static String getSource(List<String> locations, String fileName)
	{
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
					source = searchFileInZip(lf, fileName);

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

		if (sourceFile.exists())
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

	public static String searchFileInZip(File zipFile, String fileName)
	{
		String result = null;

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
						sb.append(line).append(NEW_LINEFEED);
						line = reader.readLine();
					}

					result = sb.toString();
				}
			}
		}
		catch (IOException ioe)
		{
			logger.error("", ioe);
		}

		return result;
	}
}
