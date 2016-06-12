/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.freqinlinesize;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOUBLE_QUOTE;
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
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class FreqInlineSizeOperation implements IJarScanOperation
{
	private int freqInlineSize;

	private Map<MemberSignatureParts, Integer> countMap = new HashMap<>();

	public FreqInlineSizeOperation(int freqInlineSize)
	{
		this.freqInlineSize = freqInlineSize;
	}

	@Override
	public String getReport()
	{
		List<Map.Entry<MemberSignatureParts, Integer>> sortedList = new ArrayList<>(countMap.entrySet());

		Collections.sort(sortedList, new Comparator<Map.Entry<MemberSignatureParts, Integer>>()
		{
			@Override
			public int compare(Map.Entry<MemberSignatureParts, Integer> o1, Map.Entry<MemberSignatureParts, Integer> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		StringBuilder builder = new StringBuilder();

		for (Map.Entry<MemberSignatureParts, Integer> entry : sortedList)
		{
			MemberSignatureParts msp = entry.getKey();
			int bytecodeSize = entry.getValue();

			String fqClassName = msp.getFullyQualifiedClassName();

			builder.append(C_DOUBLE_QUOTE);
			builder.append(StringUtil.getPackageName(fqClassName));
			builder.append(C_DOUBLE_QUOTE);
			builder.append(C_COMMA);

			builder.append(C_DOUBLE_QUOTE);
			builder.append(StringUtil.getUnqualifiedClassName(fqClassName));
			builder.append(C_DOUBLE_QUOTE);
			builder.append(C_COMMA);

			builder.append(C_DOUBLE_QUOTE);
			builder.append(msp.getMemberName());
			builder.append(C_DOUBLE_QUOTE);
			builder.append(C_COMMA);

			builder.append(C_DOUBLE_QUOTE);

			if (msp.getParamTypes().size() > 0)
			{
				for (String param : msp.getParamTypes())
				{
					builder.append(param).append(C_COMMA);
				}

				builder.deleteCharAt(builder.length() - 1);
			}
			builder.append(C_DOUBLE_QUOTE);
			builder.append(C_COMMA);

			builder.append(bytecodeSize);
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

			if (bcSize >= freqInlineSize && !S_STATIC_INIT.equals(msp.getMemberName()))
			{
				countMap.put(msp, bcSize);
			}
		}
	}
}
