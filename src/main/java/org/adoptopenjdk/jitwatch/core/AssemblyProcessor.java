/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyUtil;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssemblyProcessor
{
	private static final Logger logger = LoggerFactory.getLogger(AssemblyProcessor.class);

	private StringBuilder builder = new StringBuilder();

	private boolean assemblyStarted = false;
	private boolean methodStarted = false;
	private boolean methodInterrupted = false;

	private IMemberFinder memberFinder;

	public AssemblyProcessor(IMemberFinder memberFinder)
	{
		this.memberFinder = memberFinder;
	}

	public void handleLine(final String inLine)
	{
		String line = inLine.trim();

		if (DEBUG_LOGGING)
		{
			logger.debug("handleLine: '{}'", line);
		}

		if (line.startsWith(NATIVE_CODE_START))
		{
			if (DEBUG_LOGGING)
			{
				logger.debug("Assembly started");
			}

			assemblyStarted = true;

			if (builder.length() > 0)
			{
				complete();
			}
		}
		else if (assemblyStarted)
		{
			if (line.trim().startsWith(NATIVE_CODE_METHOD_MARK))
			{
				if (DEBUG_LOGGING)
				{
					logger.debug("Assembly method started");
				}

				methodStarted = true;

				if (!line.endsWith(S_ENTITY_APOS))
				{
					if (DEBUG_LOGGING)
					{
						logger.debug("Method signature interrupted");
					}
					methodInterrupted = true;
				}
			}
			else if (methodInterrupted && line.endsWith(S_ENTITY_APOS))
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
	}

	public void complete()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("completed assembly\n{}", builder.toString());
		}

		String asmString = builder.toString();

		int firstLineEnd = asmString.indexOf(C_NEWLINE);

		if (firstLineEnd != -1)
		{
			String firstLine = asmString.substring(0, firstLineEnd);

			String sig = ParseUtil.convertNativeCodeMethodName(firstLine);

			if (DEBUG_LOGGING)
			{
				logger.debug("Parsed assembly sig {}\nfrom {}", sig, firstLine);
			}

			IMetaMember currentMember = memberFinder.findMemberWithSignature(sig);

			if (currentMember != null)
			{
				if (DEBUG_LOGGING)
				{
					logger.debug("Found member {}", currentMember);
				}
				
				AssemblyMethod asmMethod = AssemblyUtil.parseAssembly(asmString);

				currentMember.setAssembly(asmMethod);
			}
			else
			{
				if (DEBUG_LOGGING)
				{
					logger.debug("Didn't find member for {}", sig);
				}
			}
		}

		builder.delete(0, builder.length());

		methodStarted = false;
		methodInterrupted = false;
	}
}