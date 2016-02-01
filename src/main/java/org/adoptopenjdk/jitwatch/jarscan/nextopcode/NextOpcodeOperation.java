/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.nextopcode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.jarscan.sequencecount.OpcodeSequence;
import org.adoptopenjdk.jitwatch.jarscan.sequencecount.SequenceCountOperation;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class NextOpcodeOperation extends SequenceCountOperation
{
	private Map<Opcode, NextBytecodeList> nextBytecodeMap;

	private int maxChildren = 0;

	public NextOpcodeOperation(int maxChildren)
	{
		super(2);

		this.maxChildren = maxChildren;
	}

	private void calculate()
	{
		nextBytecodeMap = new EnumMap<>(Opcode.class);

		for (Map.Entry<OpcodeSequence, Integer> entry : chainCountMap.entrySet())
		{
			OpcodeSequence sequence = entry.getKey();

			Opcode root = sequence.getOpcodeAtIndex(0);
			Opcode next = sequence.getOpcodeAtIndex(1);

			int count = entry.getValue();

			NextBytecodeList nextBytecodeList = nextBytecodeMap.get(root);

			if (nextBytecodeList == null)
			{
				nextBytecodeList = new NextBytecodeList();

				nextBytecodeMap.put(root, nextBytecodeList);
			}

			nextBytecodeList.add(new NextBytecode(next, count));
		}
	}

	public Map<Opcode, NextBytecodeList> getNextBytecodeMap()
	{
		if (nextBytecodeMap == null)
		{
			calculate();
		}

		return nextBytecodeMap;
	}

	@Override
	public String getReport()
	{
		if (nextBytecodeMap == null)
		{
			calculate();
		}

		StringBuilder builder = new StringBuilder();

		for (Map.Entry<Opcode, NextBytecodeList> entry : nextBytecodeMap.entrySet())
		{
			Opcode root = entry.getKey();

			NextBytecodeList nextBytecodeList = entry.getValue();

			int sum = nextBytecodeList.getSum();

			NumberFormat percentFormatter = NumberFormat.getPercentInstance();
			percentFormatter.setMinimumFractionDigits(1);

			int reportLines = 0;

			for (NextBytecode nextBytecode : nextBytecodeList.getList())
			{
				double percent = (double) nextBytecode.getCount() / (double) sum;

				String percentString = percentFormatter.format(percent);

				builder.append(root.getMnemonic()).append(S_COMMA).append(nextBytecode.getOpcode().getMnemonic()).append(S_COMMA)
						.append(nextBytecode.getCount()).append(S_COMMA).append(sum).append(S_COMMA).append(percentString)
						.append(S_NEWLINE);

				reportLines++;

				if (reportLines == maxChildren)
				{
					break;
				}
			}
		}

		return builder.toString();
	}
}