/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.methodsizehisto;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_STATIC_INIT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.jarscan.IJarScanOperation;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;

public class MethodSizeHistoOperation implements IJarScanOperation
{
	private Map<Integer, Integer> methodSizeMap = new HashMap<>();

	public MethodSizeHistoOperation()
	{
	}

	@Override
	public String getReport()
	{
		List<Map.Entry<Integer, Integer>> sortedList = new ArrayList<>(methodSizeMap.entrySet());

		Collections.sort(sortedList, new Comparator<Map.Entry<Integer, Integer>>()
		{
			@Override
			public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		StringBuilder builder = new StringBuilder();

		for (Map.Entry<Integer, Integer> entry : sortedList)
		{
			int bytecodeSize = entry.getKey();
			int count = entry.getValue();

			builder.append(bytecodeSize);
			builder.append(C_COMMA);
			builder.append(count);
			builder.append(S_NEWLINE);
		}

		return builder.toString();
	}

	@Override
	public void processInstructions(String className, MemberBytecode memberBytecode)
	{
		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		if (instructions != null && instructions.size() > 0)
		{
			BytecodeInstruction lastInstruction = instructions.get(instructions.size() - 1);

			// final instruction is a return for 1 byte
			int bcSize = 1 + lastInstruction.getOffset();

			MemberSignatureParts msp = memberBytecode.getMemberSignatureParts();

			if (!S_STATIC_INIT.equals(msp.getMemberName()))
			{
				Integer existingCount = methodSizeMap.get(bcSize);

				if (existingCount == null)
				{
					methodSizeMap.put(bcSize, 1);
				}
				else
				{
					methodSizeMap.put(bcSize, existingCount + 1);
				}
			}
		}
	}
}
