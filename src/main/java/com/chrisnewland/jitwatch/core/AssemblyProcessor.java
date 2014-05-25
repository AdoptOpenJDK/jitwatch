/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.core;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.assembly.AssemblyMethod;
import com.chrisnewland.jitwatch.model.assembly.AssemblyUtil;
import com.chrisnewland.jitwatch.util.ParseUtil;

public class AssemblyProcessor
{
	private StringBuilder builder = new StringBuilder();

	private boolean assemblyStarted = false;
	private boolean methodStarted = false;

	private IMemberFinder memberFinder;

	public AssemblyProcessor(IMemberFinder memberFinder)
	{
		this.memberFinder = memberFinder;
	}

	public void handleLine(String line)
	{
		if (line.startsWith(NATIVE_CODE_START))
		{
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
				methodStarted = true;
			}

			if (methodStarted)
			{
				char firstChar = line.charAt(0);

				// if not space or [ then line could be split
				// by interruption from another writer thread
				boolean spaceOrBracket = (firstChar == C_SPACE || firstChar == C_OPEN_SQUARE_BRACKET);

				if (builder.length() > 0 && spaceOrBracket)
				{
					builder.append(S_NEWLINE);
				}

				builder.append(line);
			}
		}
	}

	public void complete()
	{
		String asmString = builder.toString();

		int firstLineEnd = asmString.indexOf(C_NEWLINE);

		if (firstLineEnd != -1)
		{
			String firstLine = asmString.substring(0, firstLineEnd);

			String sig = ParseUtil.convertNativeCodeMethodName(firstLine);

			IMetaMember currentMember = memberFinder.findMemberWithSignature(sig);

			if (currentMember != null)
			{
				AssemblyMethod asmMethod = AssemblyUtil.parseAssembly(asmString);

				currentMember.setAssembly(asmMethod);
			}
		}

		builder.delete(0, builder.length());

		methodStarted = false;
	}
}