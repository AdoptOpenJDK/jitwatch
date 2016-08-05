/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

import org.adoptopenjdk.jitwatch.loader.ResourceLoader;
import org.junit.Before;
import org.junit.Test;

public class TestResourceLoader
{
	private Path tempJarPath;

	@Test
	public void testSourceLoader()
	{
		List<String> sourceLocations = new ArrayList<>();
		sourceLocations.add(tempJarPath.toString());

		String mySource = ResourceLoader.getSourceForClassName(getClass().getName(), sourceLocations);

		// a really unique comment
		// 1234567896465487946542165467987654646879764684

		assertNotNull(mySource);

		assertTrue(mySource.contains("1234567896465487946542165467987654646879764684"));
	}

	@Before
	public void run() throws IOException
	{
		Manifest manifest = new Manifest();

		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

		tempJarPath = Files.createTempFile("test", ".jar");

		JarOutputStream target = new JarOutputStream(new FileOutputStream(tempJarPath.toFile()), manifest);

		File startingDirectory = Paths.get(System.getProperty("user.dir"), "src", "test", "java").toFile();
		
		addFileToJar(startingDirectory, startingDirectory, target);

		target.close();
	}

	private void addFileToJar(File startingDir, File fileOrDirectory, JarOutputStream jarOutputStream) throws IOException
	{
		BufferedInputStream bufferedInputStream = null;

		try
		{
			if (fileOrDirectory.isDirectory())
			{
				String name = fileOrDirectory.getPath().replace(S_BACKSLASH, S_SLASH);

				if (!name.isEmpty())
				{
					if (!name.endsWith(S_SLASH))
					{
						name += S_SLASH;
					}

					name = name.substring(startingDir.toString().length() + 1);

					JarEntry entry = new JarEntry(name);

					jarOutputStream.putNextEntry(entry);

					jarOutputStream.closeEntry();
				}

				for (File nestedFile : fileOrDirectory.listFiles())
				{
					addFileToJar(startingDir, nestedFile, jarOutputStream);
				}
			}
			else
			{
				String name = fileOrDirectory.getPath().replace(S_BACKSLASH, S_SLASH);
				
				name = name.substring(startingDir.toString().length() + 1);

				JarEntry entry = new JarEntry(name);

				jarOutputStream.putNextEntry(entry);

				bufferedInputStream = new BufferedInputStream(new FileInputStream(fileOrDirectory));

				byte[] buffer = new byte[1024];

				int readBytes = bufferedInputStream.read(buffer);

				while (readBytes != -1)
				{
					jarOutputStream.write(buffer, 0, readBytes);

					readBytes = bufferedInputStream.read(buffer);
				}

				jarOutputStream.closeEntry();
			}
		}
		finally
		{
			if (bufferedInputStream != null)
			{
				bufferedInputStream.close();
			}
		}
	}
}
