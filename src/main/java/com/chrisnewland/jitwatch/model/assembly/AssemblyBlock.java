/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model.assembly;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.ArrayList;
import java.util.List;

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
	
	public List<AssemblyInstruction> getInstructions()
	{
		return instructions;
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		if (title != null)
		{
			builder.append(title).append(S_NEWLINE);
		}
		
		for (AssemblyInstruction instruction : instructions)
		{
			builder.append(instruction.toString());
		}

		return builder.toString();
	}
}