/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.sequencesearch;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOUBLE_QUOTE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.adoptopenjdk.jitwatch.jarscan.IJarScanOperation;
import org.adoptopenjdk.jitwatch.model.bytecode.BCParamNumeric;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public class SequenceSearchOperation implements IJarScanOperation
{	
	private List<FoundSequence> matchingMethods = new ArrayList<>();

	private List<Opcode> chain = new LinkedList<>();
	private List<Opcode> wantedChain = new LinkedList<>();

	public SequenceSearchOperation(String sequence)
	{
		String[] searchSequence = sequence.toLowerCase().split(S_COMMA);

		for (String mnemonic : searchSequence)
		{
			Opcode opcode = Opcode.getByMnemonic(mnemonic);

			wantedChain.add(opcode);
		}
	}

	private boolean compareChains()
	{
		boolean match = true;

		if (chain.size() == wantedChain.size())
		{
			for (int i = 0; i < chain.size(); i++)
			{
				Opcode gotOpcode = chain.get(i);
				Opcode wantOpcode = wantedChain.get(i);

				if (gotOpcode != wantOpcode)
				{
					match = false;
					break;
				}
			}
		}

		return match;
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
			boolean matched = handleChainStartingAtIndex(i, instructions);

			if (matched)
			{
				BytecodeInstruction firstInstruction = instructions.get(i);

				int startingBCI = firstInstruction.getOffset();

				FoundSequence foundSequence = new FoundSequence(startingBCI, memberBytecode.getMemberSignatureParts());

				matchingMethods.add(foundSequence);
			}			
		}
	}

	private boolean handleChainStartingAtIndex(int index, List<BytecodeInstruction> instructions)
	{
		boolean stopChain = false;
		boolean abandonChain = false;

		boolean matched = false;

		Set<Integer> visitedBCI = new HashSet<>();

		while (chain.size() < wantedChain.size())
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
				if (chain.size() == wantedChain.size())
				{
					matched = compareChains();
				}

				reset();
				break;
			}
			else if (abandonChain)
			{
				reset();
				break;
			}
			else if (chain.size() == wantedChain.size())
			{
				matched = compareChains();
				reset();
				break;
			}
		}

		return matched;
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

		Collections.sort(matchingMethods, new Comparator<FoundSequence>()
		{
			@Override
			public int compare(FoundSequence o1, FoundSequence o2)
			{				
				return o1.toString().compareTo(o2.toString());
			}
		});

		for (FoundSequence seq : matchingMethods)
		{
			builder.append(S_DOUBLE_QUOTE);
			builder.append(seq.getMemberSignatureParts().toStringSingleLine());
			builder.append(S_DOUBLE_QUOTE);
			builder.append(S_COMMA);
			builder.append(seq.getStartingBCI());
			builder.append(S_NEWLINE);
		}

		return builder.toString();
	}
}