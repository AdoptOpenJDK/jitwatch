/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.util.StringUtil;

public class AssemblyBlock
{
	private String title;
	private List<AssemblyInstruction> instructions = new ArrayList<>();

	public AssemblyBlock()
	{
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public void addInstruction(AssemblyInstruction instr)
	{
		instructions.add(instr);
	}

	public void replaceLastInstruction(AssemblyInstruction instr)
	{
		instructions.set(instructions.size() - 1, instr);
	}

	public List<AssemblyInstruction> getInstructions()
	{
		return instructions;
	}

	@Override
	public String toString()
	{
		return toString(0);
	}

	public String toString(int maxAnnotationWidth)
	{
		StringBuilder builder = new StringBuilder();

		if (title != null)
		{
			builder.append(StringUtil.repeat(C_SPACE, maxAnnotationWidth));

			builder.append(title).append(S_NEWLINE);
		}

		for (AssemblyInstruction instruction : instructions)
		{
			builder.append(instruction.toString(maxAnnotationWidth, false)).append(S_NEWLINE);
		}

		return builder.toString();
	}
}