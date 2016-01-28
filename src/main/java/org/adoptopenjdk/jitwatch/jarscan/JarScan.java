/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT_CLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.adoptopenjdk.jitwatch.jarscan.bytecodefrequency.BytecodeFrequencyTree;
import org.adoptopenjdk.jitwatch.jarscan.chains.ChainCounter;
import org.adoptopenjdk.jitwatch.jarscan.freqinline.FreqInlineCounter;
import org.adoptopenjdk.jitwatch.jarscan.invokecounter.InvokeCounter;
import org.adoptopenjdk.jitwatch.loader.BytecodeLoader;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;

public class JarScan
{
	private List<IJarScanOperation> operations = new ArrayList<>();

	private Writer writer;

	public JarScan(Writer writer)
	{
		this.writer = writer;
	}

	public void addOperation(IJarScanOperation op)
	{
		operations.add(op);
	}

	public void iterateJar(File jarFile) throws IOException
	{
		List<String> classLocations = new ArrayList<>();

		classLocations.add(jarFile.getPath());

		try (ZipFile zip = new ZipFile(jarFile))
		{
			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> list = (Enumeration<ZipEntry>) zip.entries();

			while (list.hasMoreElements())
			{
				ZipEntry entry = list.nextElement();

				String name = entry.getName();

				if (name.endsWith(S_DOT_CLASS))
				{
					String fqName = name.replace(S_SLASH, S_DOT).substring(0, name.length() - S_DOT_CLASS.length());

					process(classLocations, fqName);
				}
			}
		}

		createReport();
	}

	private void createReport() throws IOException
	{
		for (IJarScanOperation op : operations)
		{
			String report = op.getReport();

			writer.write(report);
			writer.flush();
		}
	}

	private void process(List<String> classLocations, String className)
	{
		ClassBC classBytecode = BytecodeLoader.fetchBytecodeForClass(classLocations, className);

		if (classBytecode != null)
		{
			for (MemberBytecode memberBytecode : classBytecode.getMemberBytecodeList())
			{
				for (IJarScanOperation op : operations)
				{
					try
					{
						op.processInstructions(className, memberBytecode);
					}
					catch (Exception e)
					{
						System.err.println("Could not process " + className + " " + memberBytecode.getMemberSignatureParts().getMemberName());
						System.err.println(memberBytecode.toString());
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}
		}
		else
		{
			System.err.println("An error occurred while parsing " + className + ". Please see jitwatch.out for details");
			System.exit(-1);
		}
	}

	private static void showUsage()
	{
		System.err.println("JarScan [options] <jar> [jar]...");
		System.err.println("Options:");
		System.err.println("-DmaxMethodSize=n\t\t\tFind methods with bytecode larger than n bytes");
		System.err.println("-DmaxBytecodeChain=n\t\t\tCount bytecode chains of length n");
		System.err.println("-DmaxFrequencyTreeChildren=n\t\t\tFind the n most frequent next bytecodes");
		System.err.println("-DinvokeCount\t\t\tCount methods by invoke type");
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length == 0)
		{
			showUsage();
			System.exit(-1);
		}

		Writer writer = new PrintWriter(System.out);

		JarScan scanner = new JarScan(writer);

		if (System.getProperty("maxMethodSize") != null)
		{
			int maxMethodBytes = Integer.getInteger("maxMethodSize");
			scanner.addOperation(new FreqInlineCounter(maxMethodBytes));
		}
		else if (System.getProperty("maxBytecodeChain") != null)
		{
			int maxBytecodeChain = Integer.getInteger("maxBytecodeChain");
			scanner.addOperation(new ChainCounter(maxBytecodeChain));
		}
		else if (System.getProperty("maxFrequencyTreeChildren") != null)
		{
			int maxFrequencyTreeChildren = Integer.getInteger("maxFrequencyTreeChildren");

			scanner.addOperation(new BytecodeFrequencyTree(maxFrequencyTreeChildren));
		}
		else if (System.getProperty("invokeCount") != null)
		{
			scanner.addOperation(new InvokeCounter());
		}

		if (scanner.operations.size() == 0)
		{
			// default mode is to report methods > 325 bytes
			int maxMethodBytes = Integer.getInteger("maxMethodSize", 325);
			scanner.addOperation(new FreqInlineCounter(maxMethodBytes));
		}

		for (String jar : args)
		{
			File jarFile = new File(jar);

			writer.write(jarFile.getAbsolutePath());

			writer.write(C_COLON);
			writer.write(S_NEWLINE);

			scanner.iterateJar(jarFile);

			writer.write(S_NEWLINE);
		}

		writer.flush();
		writer.close();
	}
}