/*
 * Copyright (c) 2013-2020 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_ASSEMBLY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.NATIVE_CODE_METHOD_MARK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.NATIVE_CODE_START;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_APOSTROPHE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.PackageManager;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssemblyProcessor
{
	private static final Logger logger = LoggerFactory.getLogger(AssemblyProcessor.class);

	private StringBuilder builder = new StringBuilder();

	private boolean assemblyStarted = false;
	private boolean methodStarted = false;
	private boolean methodInterrupted = false;

	private String previousLine = null;

	private String nativeAddress = null;

	private String entryAddress = null;

	private List<AssemblyMethod> assemblyMethods = new ArrayList<>();

	private Architecture architecture = null;

	public AssemblyProcessor()
	{
	}

	public List<AssemblyMethod> getAssemblyMethods()
	{
		return assemblyMethods;
	}

	public void clear()
	{
		assemblyMethods.clear();
		builder.delete(0, builder.length());
		nativeAddress = null;
		entryAddress = null;
		previousLine = null;
		assemblyStarted = false;
		methodStarted = false;
		methodInterrupted = false;
	}

	public void handleLine(final String inLine)
	{
		String line = inLine.replaceFirst("^ +", ""); // JMH ???

		line = StringUtil.replaceXMLEntities(line);

		String trimmedLine = line.trim();

		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("handleLine:{}", line);
		}

		if (line.startsWith("[Disassembling for mach"))
		{
			architecture = Architecture.parseFromLogLine(line);

			if (architecture == null)
			{
				logger.error("Could not determine architecture from '{}'", line);
			}
			else
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Detected architecture: {}", architecture);
				}
			}
		}

		if (S_HASH.equals(previousLine) && line.startsWith("{method}"))
		{
			if (DEBUG_LOGGING_ASSEMBLY)
			{
				logger.debug("fixup mangled {method} line");
			}

			line = S_HASH + S_SPACE + line;
		}

		if (trimmedLine.startsWith("total in heap"))
		{
			String possibleNativeAddress = getStartAddress(line);

			if (possibleNativeAddress != null)
			{
				nativeAddress = possibleNativeAddress.trim();
			}
		}

		if (trimmedLine.endsWith(" bytes") || trimmedLine.startsWith("main code"))
		{
			String possibleEntryAddress = getStartAddress(line);

			if (possibleEntryAddress != null)
			{
				entryAddress = possibleEntryAddress.trim();
			}
		}

		if (trimmedLine.endsWith("</print_nmethod>"))
		{
			complete();
		}

		if (line.startsWith(NATIVE_CODE_START) || line.startsWith("Compiled method") || line.startsWith(
				"----------------------------------------------------------------------"))
		{
			if (DEBUG_LOGGING_ASSEMBLY)
			{
				logger.debug("Assembly started");
			}

			assemblyStarted = true;

			if (builder.length() > 0)
			{
				complete();
			}

			String possibleNativeAddress = StringUtil.getSubstringBetween(line, NATIVE_CODE_START, S_COLON);

			if (possibleNativeAddress != null)
			{
				nativeAddress = possibleNativeAddress.trim();
			}
		}
		else if (assemblyStarted)
		{
			boolean couldBeNativeMethodMark = false;

			couldBeNativeMethodMark = line.startsWith(NATIVE_CODE_METHOD_MARK);

			if (couldBeNativeMethodMark)
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Assembly method started");
				}

				methodStarted = true;

				if (!line.endsWith(S_APOSTROPHE))
				{
					if (DEBUG_LOGGING_ASSEMBLY)
					{
						logger.debug("Method signature interrupted");
					}

					methodInterrupted = true;
				}
			}
			else if (methodInterrupted && line.endsWith(S_APOSTROPHE))
			{
				methodInterrupted = false;
			}

			if (methodStarted && line.length() > 0)
			{
				builder.append(line);

				if (!methodInterrupted)
				{
					builder.append(S_NEWLINE);
				}
			}
		}

		previousLine = line;
	}

	private String getStartAddress(String line)
	{
		String result = null;

		int startIndex = line.indexOf("[0x");

		if (startIndex != -1)
		{
			int endIndex = line.indexOf(',', startIndex);

			result = line.substring(startIndex + 1, endIndex);
		}

		return result;
	}

	public void complete()
	{
		String asmString = builder.toString().trim();

		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("complete({})", asmString.length());
		}

		if (asmString.length() > 0)
		{
			IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(architecture);

			if (parser != null)
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Using assembly parser {}", parser.getClass().getName());
				}

				AssemblyMethod assemblyMethod = parser.parseAssembly(asmString);

				assemblyMethod.setNativeAddress(nativeAddress);
				assemblyMethod.setEntryAddress(entryAddress);

				assemblyMethods.add(assemblyMethod);
			}
			else
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.error("No assembly parser found for architecture '{}'", architecture);
				}
			}
		}

		builder.delete(0, builder.length());

		methodStarted = false;
		methodInterrupted = false;
	}

	public void attachAssemblyToMembers(PackageManager packageManager)
	{
		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("Attaching {} assembly methods", assemblyMethods.size());
		}

		for (AssemblyMethod assemblyMethod : assemblyMethods)
		{
			String asmSignature = assemblyMethod.getAssemblyMethodSignature();

			MemberSignatureParts msp = null;

			IMetaMember currentMember = null;

			try
			{
				msp = MemberSignatureParts.fromAssembly(asmSignature);

				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Parsed assembly sig\n{}\nfrom {}", msp, asmSignature);
				}

				MetaClass metaClass = packageManager.getMetaClass(msp.getFullyQualifiedClassName());

				if (metaClass != null)
				{
					currentMember = metaClass.getMemberForSignature(msp);
				}
				else
				{
					if (DEBUG_LOGGING)
					{
						logger.debug("No MetaClass found for {}", msp.getFullyQualifiedClassName());
					}
				}
			}
			catch (LogParseException e)
			{
				logger.error("Could not parse MSP from line: {}", asmSignature, e);
			}

			if (currentMember != null)
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Found member {}", currentMember);
				}

				currentMember.addAssembly(assemblyMethod);

				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Set assembly on member {} {}", currentMember, assemblyMethod.toString());
				}
			}
			else
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Didn't find member for\n{}", msp);
				}
			}
		}
	}
}