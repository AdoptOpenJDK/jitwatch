/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_ASSEMBLY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.NATIVE_CODE_METHOD_MARK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.NATIVE_CODE_START;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_APOSTROPHE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;

import java.util.ArrayList;
import java.util.List;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COLON;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.PackageManager;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyUtil;
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

	private List<AssemblyMethod> assemblyMethods = new ArrayList<>();

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
		previousLine = null;
		assemblyStarted = false;
		methodStarted = false;
		methodInterrupted = false;
	}

	public void handleLine(final String inLine)
	{
		String line = inLine.replaceFirst("^ +", "");

		line = StringUtil.replaceXMLEntities(line);

		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("handleLine:{}", line);
		}

		if (S_HASH.equals(previousLine) && line.startsWith("{method}"))
		{
			if (DEBUG_LOGGING_ASSEMBLY)
			{
				logger.debug("fixup mangled {method} line");
			}

			line = S_HASH + S_SPACE + line;
		}

		if (line.startsWith(NATIVE_CODE_START))
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

			nativeAddress = StringUtil.getSubstringBetween(line, NATIVE_CODE_START, S_COLON).trim();
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

	public void complete()
	{
		if (DEBUG_LOGGING_ASSEMBLY)
		{
			// logger.debug("completed assembly\n{}", builder.toString());
		}

		String asmString = builder.toString().trim();

		if (asmString.length() > 0)
		{
			AssemblyMethod assemblyMethod = AssemblyUtil.parseAssembly(asmString);

			assemblyMethod.setNativeAddress(nativeAddress);

			assemblyMethods.add(assemblyMethod);
		}

		builder.delete(0, builder.length());

		methodStarted = false;
		methodInterrupted = false;
	}

	public void attachAssemblyToMembers(PackageManager packageManager)
	{
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
					logger.debug("Parsed assembly sig {}\nfrom {}", msp, asmSignature);
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
					logger.info("Set assembly on {} {}", currentMember, assemblyMethod.toString());
				}
			}
			else
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Didn't find member for {}", msp);
				}
			}
		}
	}
}