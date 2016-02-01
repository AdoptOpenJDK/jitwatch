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

import org.adoptopenjdk.jitwatch.jarscan.allocation.AllocationCountOperation;
import org.adoptopenjdk.jitwatch.jarscan.freqinlinesize.FreqInlineSizeOperation;
import org.adoptopenjdk.jitwatch.jarscan.invokecount.InvokeCountOperation;
import org.adoptopenjdk.jitwatch.jarscan.nextopcode.NextOpcodeOperation;
import org.adoptopenjdk.jitwatch.jarscan.sequencecount.SequenceCountOperation;
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
						System.err.println(
								"Could not process " + className + " " + memberBytecode.getMemberSignatureParts().getMemberName());
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
		StringBuilder builder = new StringBuilder();

		builder.append("JarScan --mode=<mode> [params] <jars>").append(S_NEWLINE);
		builder.append("--------------------------------------------------------------------------------------").append(S_NEWLINE);
		builder.append("Available modes:").append(S_NEWLINE);
		builder.append("--------------------------------------------------------------------------------------").append(S_NEWLINE);
		builder.append("  maxMethodSize      List every method with bytecode larger than specified limit.").append(S_NEWLINE);
		builder.append("    [--limit=n]      Report methods larger than n bytes.").append(S_NEWLINE);
		builder.append("--------------------------------------------------------------------------------------").append(S_NEWLINE);
		builder.append("  invokeCount        Count the most called methods for each invoke opcode.").append(S_NEWLINE);
		builder.append("    [--limit=n]      Limit to top n results per invoke type.").append(S_NEWLINE);
		builder.append("--------------------------------------------------------------------------------------").append(S_NEWLINE);
		builder.append("  nextOpcodeFreq     List the next instructions for each opcode by frequency.").append(S_NEWLINE);
		builder.append("    [--limit=n]      Limit to top n results per opcode.").append(S_NEWLINE);
		builder.append("--------------------------------------------------------------------------------------").append(S_NEWLINE);
		builder.append("  sequenceCount      Count occurences of bytecode sequences.").append(S_NEWLINE);
		builder.append("    [--length=n]     Report sequences of length n.").append(S_NEWLINE);
		builder.append("--------------------------------------------------------------------------------------").append(S_NEWLINE);
		builder.append("  allocations        Count the most allocated types.").append(S_NEWLINE);
		builder.append("    [--limit=n]      Limit to top n results.").append(S_NEWLINE);
		builder.append("--------------------------------------------------------------------------------------").append(S_NEWLINE);

		System.err.println(builder.toString());
	}

	private static final String ARG_MODE = "--mode=";
	private static final String ARG_LIMIT = "--limit=";
	private static final String ARG_LENGTH = "--length=";

	private static int getParam(String arg, String paramName)
	{
		int result = -1;

		String argValue = arg.substring(paramName.length(), arg.length());
		
		try
		{
			result = Integer.parseInt(argValue);
		}
		catch (NumberFormatException nfe)
		{
			System.err.println("Could not parse parameter " + paramName + " : " + argValue);
		}

		return result;
	}

	private static IJarScanOperation getJarScanOperation(String[] args)
	{
		IJarScanOperation operation = null;

		if (args.length >= 2)
		{
			String mode = args[0];
			String param = args[1];

			if (mode.startsWith(ARG_MODE))
			{
				String modeParam = mode.substring(ARG_MODE.length(), mode.length());
				
				switch (modeParam)
				{
				case "maxMethodSize":
				{
					int paramValue = getParam(param, ARG_LIMIT);
					if (paramValue > 0)
					{
						operation = new FreqInlineSizeOperation(paramValue);
					}
				}
					break;
				case "invokeCount":
				{
					int paramValue = getParam(param, ARG_LIMIT);
					if (paramValue > 0)
					{
						operation = new InvokeCountOperation(paramValue);
					}
				}
					break;
				case "nextOpcodeFreq":
				{
					int paramValue = getParam(param, ARG_LIMIT);
					if (paramValue > 0)
					{
						operation = new NextOpcodeOperation(paramValue);
					}
				}
					break;
				case "sequenceCount":
				{
					int paramValue = getParam(param, ARG_LENGTH);
					if (paramValue > 0)
					{
						operation = new SequenceCountOperation(paramValue);
					}
				}
					break;
				case "allocations":
				{
					int paramValue = getParam(param, ARG_LIMIT);
					if (paramValue > 0)
					{
						operation = new AllocationCountOperation(paramValue);
					}
				}
					break;
				}
			}
		}

		return operation;
	}

	public static void main(String[] args) throws IOException
	{
		IJarScanOperation operation = getJarScanOperation(args);

		if (operation == null)
		{
			showUsage();
			System.exit(-1);
		}

		Writer writer = new PrintWriter(System.out);

		JarScan scanner = new JarScan(writer);

		scanner.addOperation(operation);

		for (String arg : args)
		{
			if (arg.startsWith("--"))
			{
				continue;
			}

			File jarFile = new File(arg);

			if (jarFile.exists() && jarFile.isFile())
			{
				writer.write(jarFile.getAbsolutePath());

				writer.write(C_COLON);
				writer.write(S_NEWLINE);

				scanner.iterateJar(jarFile);

				writer.write(S_NEWLINE);
			}
			else
			{
				System.err.println("Could not scan jar " + jarFile.toString());
			}
		}

		writer.flush();
		writer.close();
	}
}