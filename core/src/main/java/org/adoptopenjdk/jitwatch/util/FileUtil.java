/*
 * Copyright (c) 2013-2020 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_BACKSLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtil
{
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	private FileUtil()
	{
	}

	public static boolean isRunningFromJar()
	{
		URL url = FileUtil.class.getResource("FileUtil.class");

		return url != null && url.toString()
								 .startsWith("jar");
	}

	public static void copyFilesFromJarToDir(String folderInJar, File targetDir)
	{
		try
		{
			final File jarFile = new File(FileUtil.class.getProtectionDomain()
														.getCodeSource()
														.getLocation()
														.getPath());

			if (jarFile.isFile())
			{
				JarFile jar = new JarFile(jarFile);

				Enumeration<JarEntry> entries = jar.entries();

				while (entries.hasMoreElements())
				{
					JarEntry entry = entries.nextElement();

					String entryName = entry.getName();

					if (entryName.startsWith(folderInJar + "/") && !entry.isDirectory())
					{
						File newFile = newFile(targetDir, entry);

						try (InputStream inputStream = jar.getInputStream(entry))
						{
							Files.copy(inputStream, newFile.toPath());

							logger.info("Wrote: {}", newFile);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Could not copy files from jar folder {} to {}", folderInJar, targetDir, e);
		}
	}

	private static File newFile(File destinationDir, JarEntry jarEntry) throws IOException
	{
		File entryFile = new File(jarEntry.getName());

		return new File(destinationDir, entryFile.getName());
	}

	public static void copyFilesToDir(File sourceDir, File targetDir)
	{
		if (sourceDir.exists() && sourceDir.isDirectory())
		{
			File[] sourceFiles = sourceDir.listFiles();

			if (DEBUG_LOGGING)
			{
				logger.info("Copying {} files", sourceFiles.length);
			}

			for (File exampleFile : sourceFiles)
			{
				try
				{
					Path srcPath = exampleFile.toPath();
					Path dstPath = targetDir.toPath()
											.resolve(exampleFile.getName());

					if (DEBUG_LOGGING)
					{
						logger.info("Copying file {} -> {}", srcPath, dstPath);
					}

					Files.copy(srcPath, dstPath);
				}
				catch (IOException ioe)
				{
					logger.error("Could not copy {} to {}", exampleFile, targetDir, ioe);
				}
			}
		}
		else
		{
			logger.error("Could not find source directory {}", sourceDir);
		}
	}

	public static File writeSource(File sourceDir, String fqClassName, String sourceCode) throws IOException
	{
		String[] parts = fqClassName.split(S_BACKSLASH + S_DOT);

		StringBuilder builder = new StringBuilder();

		builder.append(sourceDir.getAbsolutePath())
			   .append(File.separatorChar);

		for (String part : parts)
		{
			builder.append(part)
				   .append(File.separatorChar);
		}

		builder.deleteCharAt(builder.length() - 1);

		builder.append(".java");

		String filePathString = builder.toString();

		int lastSep = filePathString.lastIndexOf(File.separatorChar);

		File sourceFile;

		if (lastSep != -1)
		{
			String dirPart = filePathString.substring(0, lastSep);
			String filePart = filePathString.substring(lastSep + 1);
			File dir = new File(dirPart);

			if (!dir.exists())
			{
				dir.mkdirs();
			}

			sourceFile = new File(dir, filePart);
		}
		else
		{
			sourceFile = new File(filePathString);
		}

		if (DEBUG_LOGGING)
		{
			logger.debug("Writing source file: {}", sourceFile.getAbsolutePath());
		}

		BufferedWriter fout = new BufferedWriter(new FileWriter(sourceFile));

		try
		{
			fout.write(sourceCode);
			fout.flush();
		}
		finally
		{
			fout.close();
		}

		return sourceFile;
	}

	public static void emptyDir(File directory)
	{
		if (directory.exists() && directory.isDirectory())
		{
			File[] contents = directory.listFiles();

			for (File file : contents)
			{
				if (file.isDirectory())
				{
					emptyDir(file);
					file.delete();
				}
				else
				{
					file.delete();
				}
			}
		}
	}

	public static File getJDKSourceZip()
	{
		String javaHome = System.getProperty("java.home");

		String srcDotZip = "src.zip";

		List<Path> possiblePaths = new ArrayList<>();

		possiblePaths.add(Paths.get(javaHome, srcDotZip));
		possiblePaths.add(Paths.get(javaHome, "jre", srcDotZip));
		possiblePaths.add(Paths.get(javaHome, "jre", "lib", srcDotZip));
		possiblePaths.add(Paths.get(javaHome, "lib", srcDotZip));

		if (javaHome.contains("jre"))
		{
			possiblePaths.add(Paths.get(javaHome, "..", srcDotZip));
		}

		File result = null;

		for (Path path : possiblePaths)
		{
			File file = path.toFile();

			if (file.exists() && file.isFile())
			{
				result = file;
				break;
			}
		}

		return result;
	}
}