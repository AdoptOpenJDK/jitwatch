/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT_CLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ASTERISK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.adoptopenjdk.jitwatch.jarscan.allocationcount.AllocationCountOperation;
import org.adoptopenjdk.jitwatch.jarscan.freqinlinesize.FreqInlineSizeOperation;
import org.adoptopenjdk.jitwatch.jarscan.instructioncount.InstructionCountOperation;
import org.adoptopenjdk.jitwatch.jarscan.invokecount.InvokeCountOperation;
import org.adoptopenjdk.jitwatch.jarscan.methodlength.MethodLengthOperation;
import org.adoptopenjdk.jitwatch.jarscan.methodsizehisto.MethodSizeHistoOperation;
import org.adoptopenjdk.jitwatch.jarscan.nextinstruction.NextInstructionOperation;
import org.adoptopenjdk.jitwatch.jarscan.sequencecount.SequenceCountOperation;
import org.adoptopenjdk.jitwatch.jarscan.sequencesearch.SequenceSearchOperation;
import org.adoptopenjdk.jitwatch.loader.BytecodeLoader;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;

public class JarScan
{
	private IJarScanOperation operation;
	private List<String> allowedPackagePrefixes = new ArrayList<>();

	public JarScan(IJarScanOperation operation)
	{
		this.operation = operation;
	}

	public void writeReport()
	{
		Writer writer = new PrintWriter(System.out);

		String report = operation.getReport();

		try
		{
			writer.write(report);
			writer.flush();
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
	}

	public void addAllowedPackagePrefix(String prefix)
	{
		allowedPackagePrefixes.add(prefix);
	}

	private boolean isAllowedPackage(String fqClassName)
	{
		boolean allowed = false;

		if (allowedPackagePrefixes.size() == 0)
		{
			allowed = true;
		}
		else
		{
			for (String allowedPrefix : allowedPackagePrefixes)
			{
				if (fqClassName.startsWith(allowedPrefix))
				{
					allowed = true;
					break;
				}
			}
		}

		return allowed;
	}

	private void process(List<String> classLocations, String fqClassName)
	{
		if (!isAllowedPackage(fqClassName))
		{
			return;
		}

		boolean cacheBytecode = false;

		ClassBC classBytecode = BytecodeLoader.fetchBytecodeForClass(classLocations, fqClassName, cacheBytecode);

		if (classBytecode != null)
		{
			for (MemberBytecode memberBytecode : classBytecode.getMemberBytecodeList())
			{
				try
				{
					operation.processInstructions(fqClassName, memberBytecode);
				}
				catch (Exception e)
				{
					System.err.println(
							"Could not process " + fqClassName + " " + memberBytecode.getMemberSignatureParts().getMemberName());
					System.err.println(memberBytecode.toString());
					e.printStackTrace();
					System.exit(-1);
				}

			}
		}
		else
		{
			System.err.println("An error occurred while parsing " + fqClassName);
		}
	}

	private static void showUsage()
	{
		StringBuilder builder = new StringBuilder();

		String SEPARATOR = "---------------------------------------------------------------------------------------------------";

		builder.append("JarScan --mode=<mode> [options] [params] <jars>").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("Options:").append(S_NEWLINE);
		builder.append("     --packages=a,b,c     Only include methods from named packages. E.g. --packages=java.util.*")
				.append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("Modes:").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("  maxMethodSize            List every method with bytecode larger than specified limit.").append(S_NEWLINE);
		builder.append("     --limit=n             Report methods larger than n bytes.").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("  sequenceCount            Count instruction sequences.").append(S_NEWLINE);
		builder.append("     --length=n            Report sequences of length n.").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("  invokeCount              Count the most called methods for each invoke instruction.").append(S_NEWLINE);
		builder.append("    [--limit=n]            Limit to top n results per invoke type.").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("  nextInstructionFreq      List the most popular next instruction for each bytecode instruction.")
				.append(S_NEWLINE);
		builder.append("    [--limit=n]            Limit to top n results per instruction.").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("  allocationCount          Count the most allocated types.").append(S_NEWLINE);
		builder.append("    [--limit=n]            Limit to top n results.").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("  instructionCount         Count occurences of each bytecode instruction.").append(S_NEWLINE);
		builder.append("    [--limit=n]            Limit to top n results.").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("  sequenceSearch           List methods containing the specified bytecode sequence.").append(S_NEWLINE);
		builder.append("     --sequence=a,b,c,...  Comma separated sequence of bytecode instructions.").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("  methodSizeHisto          List frequencies of method bytecode sizes.").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);
		builder.append("  methodLength             List methods of the given bytecode size.").append(S_NEWLINE);
		builder.append("    --length=n             Size of methods to find.").append(S_NEWLINE);
		builder.append(SEPARATOR).append(S_NEWLINE);

		System.err.println(builder.toString());
	}

	private static final String ARG_PACKAGES = "--packages=";
	private static final String ARG_MODE = "--mode=";
	private static final String ARG_LIMIT = "--limit=";
	private static final String ARG_LENGTH = "--length=";
	private static final String ARG_SEQUENCE = "--sequence=";

	private static int getParam(String[] args, String paramName, boolean mandatory)
	{
		int result;

		if (!mandatory)
		{
			result = 0;
		}
		else
		{
			result = -1;
		}

		for (String param : args)
		{
			if (param.startsWith(paramName))
			{
				String argValue = param.substring(paramName.length(), param.length());

				try
				{
					result = Integer.parseInt(argValue);
				}
				catch (NumberFormatException nfe)
				{
					System.err.println("Could not parse parameter " + paramName + " : " + argValue);
				}

				break;
			}
		}

		return result;
	}

	private static String getParamString(String[] args, String paramName)
	{
		String result = null;

		for (String param : args)
		{
			if (param.startsWith(paramName))
			{
				result = param.substring(paramName.length(), param.length());
				break;
			}
		}

		return result;
	}

	private static IJarScanOperation getJarScanOperation(String[] args)
	{
		IJarScanOperation operation = null;

		String mode = getParamString(args, ARG_MODE);

		if (mode != null)
		{
			String modeParam = mode.toLowerCase();

			switch (modeParam)
			{
			case "maxmethodsize":
			{
				int paramValue = getParam(args, ARG_LIMIT, true);

				if (paramValue > 0)
				{
					operation = new FreqInlineSizeOperation(paramValue);
				}
				break;
			}
			case "sequencecount":
			{
				int paramValue = getParam(args, ARG_LENGTH, true);

				if (paramValue > 0)
				{
					operation = new SequenceCountOperation(paramValue);
				}
				break;
			}
			case "invokecount":
			{
				int paramValue = getParam(args, ARG_LIMIT, false);

				if (paramValue >= 0)
				{
					operation = new InvokeCountOperation(paramValue);
				}
				break;
			}
			case "nextinstructionfreq":
			{
				int paramValue = getParam(args, ARG_LIMIT, false);

				if (paramValue >= 0)
				{
					operation = new NextInstructionOperation(paramValue);
				}
			}
				break;
			case "allocationcount":
			{
				int paramValue = getParam(args, ARG_LIMIT, false);
				if (paramValue >= 0)
				{
					operation = new AllocationCountOperation(paramValue);
				}
				break;
			}
			case "instructioncount":
			{
				int paramValue = getParam(args, ARG_LIMIT, false);

				if (paramValue >= 0)
				{
					operation = new InstructionCountOperation(paramValue);
				}
				break;
			}
			case "sequencesearch":
			{
				String sequence = getParamString(args, ARG_SEQUENCE);

				if (sequence != null)
				{
					operation = new SequenceSearchOperation(sequence);
				}
				break;
			}
			case "methodsizehisto":
			{
				operation = new MethodSizeHistoOperation();
				break;
			}
			case "methodlength":
			{
				int paramValue = getParam(args, ARG_LENGTH, true);

				if (paramValue > 0)
				{
					operation = new MethodLengthOperation(paramValue);
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

		JarScan scanner = new JarScan(operation);

		String packages = getParamString(args, ARG_PACKAGES);

		if (packages != null)
		{
			String[] prefixes = packages.split(S_COMMA);

			for (String prefix : prefixes)
			{
				prefix = prefix.replace(S_ASTERISK, S_EMPTY);
				scanner.addAllowedPackagePrefix(prefix);
			}
		}

		for (String arg : args)
		{
			if (arg.startsWith("--"))
			{
				continue;
			}

			File jarFile = new File(arg);

			if (jarFile.exists() && jarFile.isFile())
			{
				scanner.iterateJar(jarFile);
			}
			else
			{
				System.err.println("Could not scan jar " + jarFile.toString());
			}
		}

		scanner.writeReport();
	}
}