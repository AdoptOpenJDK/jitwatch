/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_ASSEMBLY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOUBLE_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPTIMIZED_VIRTUAL_CALL;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.adoptopenjdk.jitwatch.optimizedvcall.VirtualCallSite;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssemblyInstruction
{
	private String annotation;
	private long address;
	private String modifier;
	private String mnemonic;
	private List<String> operands = new ArrayList<>();
	private List<String> commentLines = new ArrayList<>();
	private final AssemblyLabels labels;

	private static final Pattern PATTERN_ASSEMBLY_CALL_SIG = Pattern.compile("^; - (.*)::(.*)@(.*)\\s\\(line\\s(.*)\\)");

	private static final Logger logger = LoggerFactory.getLogger(AssemblyInstruction.class);

	public AssemblyInstruction(String annotation, long address, String modifier, String mnemonic, List<String> operands,
			String firstComment, AssemblyLabels labels)
	{
		this.annotation = annotation;
		this.address = address;
		this.modifier = modifier;
		this.mnemonic = mnemonic;
		this.operands = operands;
		this.labels = labels;

		if (firstComment != null)
		{
			this.commentLines.add(firstComment.trim());
		}
	}

	public String getAnnotation()
	{
		return annotation;
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

	public List<String> getCommentLines()
	{
		return commentLines;
	}

	public void addCommentLine(String comment)
	{
		if (comment != null)
		{
			commentLines.add(comment.trim());
		}
	}

	public boolean isOptimizedVCall()
	{
		boolean result = false;

		int commentLineCount = commentLines.size();

		if (commentLineCount > 1)
		{
			String lastLine = commentLines.get(commentLineCount - 1);

			if (lastLine.contains(S_OPTIMIZED_VIRTUAL_CALL))
			{
				result = true;
			}
		}

		return result;
	}

	public VirtualCallSite getOptimizedVirtualCallSiteOrNull()
	{
		VirtualCallSite result = null;

		if (isOptimizedVCall())
		{
			// Oop comment
			// *invoke comment
			// callsite comment+
			// optimized virtual_call

			String callSiteCommentLine = commentLines.get(2);

			Matcher matcher = PATTERN_ASSEMBLY_CALL_SIG.matcher(callSiteCommentLine);

			if (matcher.find())
			{
				String className = matcher.group(1);
				String methodName = matcher.group(2);
				String bytecodeOffset = matcher.group(3);
				String lineNumber = matcher.group(4);

				try
				{
					result = new VirtualCallSite(className, methodName, Integer.parseInt(bytecodeOffset),
							Integer.parseInt(lineNumber));
				}
				catch (NumberFormatException nfe)
				{
					if (DEBUG_LOGGING_ASSEMBLY)
					{
						logger.warn("Could not parse CallSite from line: {}", callSiteCommentLine);
					}
				}
			}
		}

		return result;
	}

	@Override
	public String toString()
	{
		return toString(0);
	}

	public String toString(int annoWidth)
	{
		StringBuilder builder = new StringBuilder();

		builder.append(StringUtil.alignLeft(annotation, annoWidth));
		labels.formatAddress(address, builder);
		builder.append(C_COLON).append(C_SPACE);

		if (modifier != null)
		{
			builder.append(modifier);
			builder.append(C_SPACE);
		}

		builder.append(mnemonic);

		labels.formatOperands(this, builder);

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

		return StringUtil.rtrim(builder.toString());
	}

	// Allow splitting an instruction with a multi-line comment across multiple
	// labels which all contain the instruction
	public String toString(int annoWidth, int line)
	{
		StringBuilder builder = new StringBuilder();

		builder.append(StringUtil.alignLeft(annotation, annoWidth));
		labels.formatAddress(address, builder);
		builder.append(C_COLON).append(C_SPACE);

		if (modifier != null)
		{
			builder.append(modifier);
			builder.append(C_SPACE);
		}

		builder.append(mnemonic);

		labels.formatOperands(this, builder);

		int lineLength = builder.length();

		if (commentLines.size() > 0)
		{
			if (line == 0)
			{
				// first comment on same line as instruction
				builder.append(S_DOUBLE_SPACE).append(commentLines.get(0)).append(S_NEWLINE);
			}
			else
			{
				// later comments on own line
				builder.delete(0, builder.length());
				builder.append(StringUtil.repeat(C_SPACE, lineLength + 2));
				builder.append(commentLines.get(line)).append(S_NEWLINE);
			}
		}
		else
		{
			builder.append(S_NEWLINE);
		}

		return StringUtil.rtrim(builder.toString());
	}
}
