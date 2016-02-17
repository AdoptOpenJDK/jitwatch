/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.nextinstruction;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.jarscan.sequencecount.InstructionSequence;
import org.adoptopenjdk.jitwatch.jarscan.sequencecount.SequenceCountOperation;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class NextInstructionOperation extends SequenceCountOperation
{
	private Map<Opcode, NextInstructionCountList> nextBytecodeMap;

	private int maxChildren = 0;

	public NextInstructionOperation(int maxChildren)
	{
		super(2);

		this.maxChildren = maxChildren;
	}

	private void calculate()
	{
		nextBytecodeMap = new EnumMap<>(Opcode.class);

		for (Map.Entry<InstructionSequence, Integer> entry : chainCountMap.entrySet())
		{
			InstructionSequence sequence = entry.getKey();

			Opcode root = sequence.getOpcodeAtIndex(0);
			Opcode next = sequence.getOpcodeAtIndex(1);

			int count = entry.getValue();

			NextInstructionCountList nextBytecodeList = nextBytecodeMap.get(root);

			if (nextBytecodeList == null)
			{
				nextBytecodeList = new NextInstructionCountList();

				nextBytecodeMap.put(root, nextBytecodeList);
			}

			nextBytecodeList.add(new NextInstructionCount(next, count));
		}
	}

	public Map<Opcode, NextInstructionCountList> getNextBytecodeMap()
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
		
		List<Map.Entry<Opcode, NextInstructionCountList>> sortedList = new ArrayList<>(nextBytecodeMap.entrySet());

		Collections.sort(sortedList, new Comparator<Map.Entry<Opcode, NextInstructionCountList>>()
		{
			@Override
			public int compare(Map.Entry<Opcode, NextInstructionCountList> o1, Map.Entry<Opcode, NextInstructionCountList> o2)
			{
				return o1.getKey().getMnemonic().compareTo(o2.getKey().getMnemonic());
			}
		});

		for (Map.Entry<Opcode, NextInstructionCountList> entry : sortedList)
		{
			Opcode root = entry.getKey();

			NextInstructionCountList nextBytecodeList = entry.getValue();

			int reportLines = 0;

			for (NextInstructionCount nextBytecode : nextBytecodeList.getList())
			{
				builder.append(root.getMnemonic()).append(S_COMMA).append(nextBytecode.getOpcode().getMnemonic()).append(S_COMMA)
						.append(nextBytecode.getCount()).append(S_NEWLINE);

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