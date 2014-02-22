package com.chrisnewland.jitwatch.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.*;

import com.chrisnewland.jitwatch.loader.BytecodeLoader;

public class JarScan
{
	private static StringBuilder result = new StringBuilder();

	public static void iterateJar(File jarFile, int maxMethodBytes) throws IOException
	{
		ZipFile zip = new ZipFile(jarFile);

		List<String> classLocations = new ArrayList<>();

		classLocations.add(jarFile.getPath());

		try
		{
			Enumeration<ZipEntry> list = (Enumeration<ZipEntry>) zip.entries();

			while (list.hasMoreElements())
			{
				ZipEntry entry = list.nextElement();

				String name = entry.getName();

				if (name.endsWith(".class"))
				{
					String fqName = name.replace("/", ".").substring(0, name.length() - 6);

					process(classLocations, fqName, maxMethodBytes);
				}
			}
		}
		finally
		{
			zip.close();
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("results.txt")));
		writer.write(result.toString());
		writer.flush();
		writer.close();
	}

	private static void process(List<String> classLocations, String className, int maxMethodBytes)
	{
		Map<String, String> methodBytecode = BytecodeLoader.fetchByteCodeForClass(classLocations, className);

		boolean shownClass = false;

		for (Map.Entry<String, String> entry : methodBytecode.entrySet())
		{
			String methodName = entry.getKey();

			String bytecode = entry.getValue();

			String[] lines = bytecode.split("\n");

			String lastLine = lines[lines.length - 1];

			String[] lastLineParts = lastLine.split(" ");

			String bcOffset = lastLineParts[0].substring(0, lastLineParts[0].length() - 1);

			try
			{
				int bcSize = Integer.parseInt(bcOffset);
				bcSize++;

				if (bcSize >= maxMethodBytes && !methodName.equals("static {}"))
				{
					if (!shownClass)
					{
						result.append(className).append("\n");
						shownClass = true;
					}

					result.append(bcSize + " -> " + methodName).append("\n");
				}
			}
			catch (NumberFormatException nfe)
			{

			}

		}

	}

	public static void main(String[] args) throws IOException
	{
		File jarFile = new File("/home/chris/jdk1.7.0_51/jre/lib/rt.jar");
		int maxMethodBytes = 325;
		iterateJar(jarFile, maxMethodBytes);
	}
}
