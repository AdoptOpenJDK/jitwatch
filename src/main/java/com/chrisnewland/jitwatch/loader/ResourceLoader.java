/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.loader;

import com.chrisnewland.jitwatch.model.MetaClass;
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

import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_DOT;

public class ResourceLoader
{
    private static final Logger logger = LoggerFactory.getLogger(ResourceLoader.class);

	public static String getSourceFilename(MetaClass metaClass)
	{
		String fqName = metaClass.getFullyQualifiedName();
		
		int dollarPos = fqName.indexOf("$");

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
					source = searchFileInDirectory(lf, fileName);

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

	public static String searchFileInDirectory(File dir, String fileName)
	{
		String result = null;

		File sourceFile = new File(dir, fileName);

		if (sourceFile.exists())
		{
			try
			{
				byte[] fileBytes = Files.readAllBytes(sourceFile.toPath());
				result = new String(fileBytes, StandardCharsets.UTF_8);
			}
			catch (IOException ioe)
			{
                logger.error(String.format("Exception: %s", ioe.getMessage()), ioe);
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
						sb.append(line).append("\n");
						line = reader.readLine();
					}

					result = sb.toString();
				}
			}
		}
		catch (IOException ioe)
		{
            logger.error(String.format("Exception: %s", ioe.getMessage()), ioe);
		}

		return result;
	}
}
