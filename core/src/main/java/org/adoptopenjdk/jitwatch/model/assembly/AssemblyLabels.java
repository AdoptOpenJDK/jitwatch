/*
 * Copyright (c) 2016 Chris Newland.
 * Copyright (c) 2015 Philip Aston
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HEX_PREFIX;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.adoptopenjdk.jitwatch.util.StringUtil;

/**
 * Calculate symbolic names for method-local addresses.
 * 
 * <p>
 * Operates in two modes. During parsing, {@link #newInstruction(AssemblyInstruction)} accumulates
 * addresses used by instructions. After all the instructions have been parsed,
 * {@link #buildLabels()} builds an index of addresses of all known instructions
 * to label numbers, and {@link #formatAddress} and {@link #formatOperands}
 * replace matching addresses with their labels.
 *
 * @author Philip Aston
 */
public final class AssemblyLabels
{
	private SortedSet<Long> addresses = new TreeSet<>();
	private final Map<Long, Short> labels = new HashMap<>();

	private long lowest = Long.MAX_VALUE;
	private long highest;

	public void newInstruction(AssemblyInstruction instruction)
	{
		final long address = instruction.getAddress();

		lowest = Math.min(lowest, address);
		highest = Math.max(highest, address);

		final Long l = instructionToLabel(instruction);

		if (l != null)
		{
			addresses.add(l);
		}
	}

	private Long instructionToLabel(AssemblyInstruction instruction)
	{
		final List<String> operands = instruction.getOperands();

		if (instruction.getMnemonic().startsWith("j") && operands.size() == 1)
		{
			try
			{
				return AssemblyUtil.getValueFromAddress(operands.get(0));
			}
			catch (NumberFormatException nfe)
			{
				// could be Intel format jump to Stub:: reference
			}
		}

		return null;
	}

	public void buildLabels()
	{
		short next = 0;

		if (lowest != Long.MAX_VALUE)
		{
			for (Long a : addresses.subSet(lowest, highest + 1))
			{
				labels.put(a, next++);
			}
		}

		addresses = null;
	}

	public void formatAddress(long address, StringBuilder builder)
	{
		final Short label = labels.get(address);

		if (label != null)
		{
			builder.append(StringUtil.pad(String.format("L%04x", label), 18, C_SPACE, true));
		}
		else
		{
			builder.append(S_HEX_PREFIX);
			builder.append(StringUtil.pad(Long.toHexString(address), 16, '0', true));
		}
	}

	public void formatOperands(AssemblyInstruction instruction, StringBuilder builder)
	{
		final Long address = instructionToLabel(instruction);
		
		final Short label = labels.get(address);

		if (label != null)
		{
			builder.append(C_SPACE);
			builder.append(String.format("L%04x", label));
		}
		else
		{
			final List<String> operands = instruction.getOperands();

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
	}
}
