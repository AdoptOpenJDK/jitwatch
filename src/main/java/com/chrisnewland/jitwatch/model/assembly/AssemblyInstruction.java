/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model.assembly;

import java.util.ArrayList;
import java.util.List;

import com.chrisnewland.jitwatch.util.StringUtil;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class AssemblyInstruction
{
	private long address; // 64 bit
	private String modifier;
	private String mnemonic;
	private List<String> operands = new ArrayList<>();
	private List<String> commentLines = new ArrayList<>();

	public AssemblyInstruction(long address, String modifier, String mnemonic, List<String> operands, String firstComment)
	{
		this.address = address;
		this.modifier = modifier;
		this.mnemonic = mnemonic;
		this.operands = operands;

		if (firstComment != null)
		{
			this.commentLines.add(firstComment.trim());
		}
	}

	public long getAddress()
	{
		return address;
	}

	public String getModifier()
	{
		return modifier;
	}

	public String getMnemonic()
	{
		return mnemonic;
	}

	public List<String> getOperands()
	{
		return operands;
	}

	public String getComment()
	{
		StringBuilder builder = new StringBuilder();

		if (commentLines.size() > 0)
		{
			for (String line : commentLines)
			{
				builder.append(line).append(S_NEWLINE);
			}

			builder.deleteCharAt(builder.length() - 1);
		}

		return builder.toString();
	}

	public void addCommentLine(String comment)
	{
		if (comment != null)
		{
			commentLines.add(comment.trim());
		}
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(ASSEMBLY_ADDRESS).append(StringUtil.pad(Long.toHexString(address), 16, '0', true));
		builder.append(C_COLON).append(C_SPACE);

		if (modifier != null)
		{
			builder.append(modifier);
			builder.append(C_SPACE);
		}

		builder.append(mnemonic);

		if (operands.size() > 0)
		{
			builder.append(C_SPACE);

			for (String op : operands)
			{
				builder.append(op).append(S_COMMA);
			}

			builder.deleteCharAt(builder.length() - 1);
		}

		int lineLength = builder.length();

		if (commentLines.size() > 0)
		{
			boolean first = true;

			for (String commentLine : commentLines)
			{
				if (first)
				{
					builder.append(S_DOUBLE_SPACE).append(commentLine).append(S_NEWLINE);
					first = false;
				}
				else
				{
					builder.append(StringUtil.repeat(C_SPACE, lineLength + 2));
					builder.append(commentLine).append(S_NEWLINE);
				}
			}
		}
		else
		{
			builder.append(S_NEWLINE);
		}

		return builder.toString();
	}
}
