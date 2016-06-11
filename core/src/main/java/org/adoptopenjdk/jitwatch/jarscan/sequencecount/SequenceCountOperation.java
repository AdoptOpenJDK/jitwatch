/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.sequencecount;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.adoptopenjdk.jitwatch.jarscan.IJarScanOperation;
import org.adoptopenjdk.jitwatch.model.bytecode.BCParamNumeric;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class SequenceCountOperation implements IJarScanOperation
{
	protected Map<InstructionSequence, Integer> chainCountMap = new TreeMap<>();

	private List<Opcode> chain = new LinkedList<>();

	private int maxLength = 0;

	public SequenceCountOperation(int maxLength)
	{
		this.maxLength = maxLength;
	}

	private void storeChain()
	{
		InstructionSequence sequence = new InstructionSequence(chain);

		Integer count = chainCountMap.get(sequence);

		if (count == null)
		{
			chainCountMap.put(sequence, 1);
		}
		else
		{
			chainCountMap.put(sequence, count + 1);
		}
	}

	public Map<InstructionSequence, Integer> getSequenceScores()
	{
		return chainCountMap;
	}

	public List<Map.Entry<InstructionSequence, Integer>> getSortedData()
	{
		List<Map.Entry<InstructionSequence, Integer>> result = new ArrayList<>(chainCountMap.entrySet());

		Collections.sort(result, new Comparator<Map.Entry<InstructionSequence, Integer>>()
		{
			@Override
			public int compare(Map.Entry<InstructionSequence, Integer> o1, Map.Entry<InstructionSequence, Integer> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		return result;
	}

	public int getCountForChain(String chain)
	{
		return chainCountMap.get(chain);
	}

	public void reset()
	{
		chain.clear();
	}

	@Override
	public void processInstructions(String className, MemberBytecode memberBytecode)
	{
		reset();

		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		for (int i = 0; i < instructions.size(); i++)
		{
			handleChainStartingAtIndex(i, instructions);
		}
	}

	private void handleChainStartingAtIndex(int index, List<BytecodeInstruction> instructions)
	{
		boolean stopChain = false;
		boolean abandonChain = false;

		Set<Integer> visitedBCI = new HashSet<>();
				
		while (chain.size() < maxLength)
		{
			BytecodeInstruction instruction = instructions.get(index);
					
			int instrBCI = instruction.getOffset();

			visitedBCI.add(instrBCI);

			Opcode opcode = instruction.getOpcode();

			// =======================
			// The Rules
			// =======================

			// *RETURN ends a chain. Chain is discarded if not required length
			// INVOKE* drops through to next bytecode
			// GOTO* is followed
			// IF*, TABLESWITCH, and LOOKUPSWITCH - drop through
			// JSR, JSR_W, RET are not followed - discard the chain
			// ATHROW ends a chain
			// loops are detected and end the parsing

			switch (opcode)
			{
			case IRETURN:
			case LRETURN:
			case FRETURN:
			case DRETURN:
			case ARETURN:
			case RETURN:
				stopChain = true;
				break;
				
			case ATHROW:
				stopChain = true;
				break;

			case JSR:
			case JSR_W:
			case RET:
				abandonChain = true;
				break;

			case GOTO:
			case GOTO_W:
				int gotoBCI = ((BCParamNumeric) instruction.getParameters().get(0)).getValue();

				if (!visitedBCI.contains(gotoBCI))
				{
					index = getIndexForBCI(instructions, gotoBCI);
				}
				break;

			default:
				index++;
				break;
			}

			chain.add(opcode);

			if (stopChain)
			{
				if (chain.size() == maxLength)
				{
					storeChain();
				}

				reset();
				break;
			}
			else if (abandonChain)
			{
				reset();
				break;
			}
			else if (chain.size() == maxLength)
			{
				storeChain();
				reset();
				break;
			}
		}
	}

	private int getIndexForBCI(List<BytecodeInstruction> instructions, int bci)
	{
		int index = -1;

		for (int i = 0; i < instructions.size(); i++)
		{
			BytecodeInstruction instruction = instructions.get(i);

			if (instruction.getOffset() == bci)
			{
				index = i;
				break;
			}
		}

		return index;
	}

	@Override
	public String getReport()
	{
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<InstructionSequence, Integer> entry : getSortedData())
		{
			builder.append(entry.getKey().toString()).append(S_COMMA).append(entry.getValue()).append(S_NEWLINE);
		}

		return builder.toString();
	}
}
