/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.chrisnewland.jitwatch.model.MetaClass;
import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class ResourceLoader
{
    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private ResourceLoader() {
    }

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
				ioe.printStackTrace();
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
			ioe.printStackTrace();
		}

		return result;
	}
}
