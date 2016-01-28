/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.invokecounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOUBLE_QUOTE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

public class InvokeCountMap
{
	private Map<String, Integer> methodCountMap = new TreeMap<>();
	
	public void count(String method)
	{
		Integer count = methodCountMap.get(method);
		
		if (count == null)
		{
			count = new Integer(1);
		}
		else
		{
			count++;
		}
		
		methodCountMap.put(method, count);
	}
	
	public String toString(Opcode prefix)
	{
		StringBuilder builder = new StringBuilder();
		
		List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(methodCountMap.entrySet());

		Collections.sort(sortedList, new Comparator<Map.Entry<String, Integer>>()
		{
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
			}
		});
	
		for (Map.Entry<String, Integer> entry : sortedList)
		{
			String methodName = entry.getKey();
			Integer count = entry.getValue();
			
			builder.append(C_DOUBLE_QUOTE).append(prefix.getMnemonic()).append(C_DOUBLE_QUOTE).append(C_COMMA);
			builder.append(C_DOUBLE_QUOTE).append(methodName).append(C_DOUBLE_QUOTE).append(C_COMMA);
			builder.append(count).append(S_NEWLINE);
		}
				
		return builder.toString();
	}
}
