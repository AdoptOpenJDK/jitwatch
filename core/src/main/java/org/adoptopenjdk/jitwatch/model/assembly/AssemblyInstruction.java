/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_ASSEMBLY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HEX_PREFIX;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOUBLE_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPTIMIZED_VIRTUAL_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SAFEPOINT_POLL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SAFEPOINT_POLL_RETURN;

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
	private List<String> prefixes;
	private String mnemonic;
	private List<String> operands = new ArrayList<>();
	private List<String> commentLines = new ArrayList<>();
	private final AssemblyLabels labels;
	private boolean isSafePoint = false;

	private static final Pattern PATTERN_ASSEMBLY_CALL_SIG = Pattern.compile("^; - (.*)::(.*)@(.*)\\s\\(line\\s(.*)\\)");

	private static final Logger logger = LoggerFactory.getLogger(AssemblyInstruction.class);

	public AssemblyInstruction(String annotation, long address, List<String> prefixes, String mnemonic, List<String> operands,
			String firstComment, AssemblyLabels labels)
	{
		this.annotation = annotation;
		this.address = address;
		this.prefixes = prefixes;
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

	public List<String> getPrefixes()
	{
		return prefixes;
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
			commentLines.add(comment);

			if (!isSafePoint)
			{
				isSafePoint = comment.contains(S_SAFEPOINT_POLL) || comment.contains(S_SAFEPOINT_POLL_RETURN);
			}
		}
	}

	public boolean isSafePoint()
	{
		return isSafePoint;
	}

	public void appendToLastCommentLine(String comment)
	{
		if (comment != null)
		{
			String lastCommentLine = commentLines.get(commentLines.size() - 1);
			commentLines.set(commentLines.size() - 1, lastCommentLine + comment);
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
		return toString(0, false);
	}

	public String toString(int annoWidth, boolean useLocalLabels)
	{
		StringBuilder builder = new StringBuilder();

		builder.append(StringUtil.alignLeft(annotation, annoWidth));

		if (useLocalLabels)
		{
			labels.formatAddress(address, builder);
		}
		else
		{
			builder.append(S_HEX_PREFIX).append(StringUtil.pad(Long.toHexString(address), 16, '0', true));
		}

		builder.append(C_COLON).append(C_SPACE);

		if (!prefixes.isEmpty())
		{
			for (String prefix : prefixes)
			{
				builder.append(prefix);
				builder.append(C_SPACE);
			}
		}

		builder.append(mnemonic);

		if (useLocalLabels)
		{
			labels.formatOperands(this, builder);
		}
		else
		{
			if (operands.size() > 0)
			{
				builder.append(C_SPACE);

				for (String op : operands)
				{
					builder.append(op).append(S_COMMA);
				}

				builder.deleteCharAt(builder.length() - 1);
			}
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

		return StringUtil.rtrim(builder.toString());
	}

	// Allow splitting an instruction with a multi-line comment across multiple
	// labels which all contain the instruction
	public String toString(int annoWidth, int line, boolean useLocalLabels)
	{
		StringBuilder builder = new StringBuilder();

		builder.append(StringUtil.alignLeft(annotation, annoWidth));

		if (useLocalLabels)
		{
			labels.formatAddress(address, builder);
		}
		else
		{
			builder.append(S_HEX_PREFIX).append(StringUtil.pad(Long.toHexString(address), 16, '0', true));
		}

		builder.append(C_COLON).append(C_SPACE);

		if (!prefixes.isEmpty())
		{
			for (String prefix : prefixes)
			{
				builder.append(prefix);
				builder.append(C_SPACE);
			}
		}

		builder.append(mnemonic);

		if (useLocalLabels)
		{
			labels.formatOperands(this, builder);
		}
		else
		{
			if (operands.size() > 0)
			{
				builder.append(C_SPACE);

				for (String op : operands)
				{
					builder.append(op).append(S_COMMA);
				}

				builder.deleteCharAt(builder.length() - 1);
			}
		}

		int lineLength = builder.length();

		if (commentLines.size() > 0)
		{
			String comment = commentLines.get(line);

			if (line == 0)
			{
				// first comment on same line as instruction
				builder.append(S_DOUBLE_SPACE).append(comment);
			}
			else
			{
				// later comments on own line
				builder.delete(0, builder.length());
				builder.append(StringUtil.repeat(C_SPACE, lineLength + 2));
				builder.append(comment);
			}

			if (comment.contains(S_SAFEPOINT_POLL) || comment.contains(S_SAFEPOINT_POLL_RETURN))
			{
				builder.append(" *** SAFEPOINT POLL ***");
			}

			builder.append(S_NEWLINE);

		}
		else
		{
			builder.append(S_NEWLINE);
		}

		return StringUtil.rtrim(builder.toString());
	}
}