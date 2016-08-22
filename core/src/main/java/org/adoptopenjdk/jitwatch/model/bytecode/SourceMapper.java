/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_TRIVIEW;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceMapper
{
	private static final Logger logger = LoggerFactory.getLogger(SourceMapper.class);

	private static Map<String, List<ClassBC>> sourceToClassMap = new HashMap<>();

	public static void clear()
	{
		sourceToClassMap.clear();
	}

	private static String getFullyQualifiedSourceName(ClassBC classBytecode)
	{
		StringBuilder builder = new StringBuilder();

		String packageName = classBytecode.getPackageName();

		if (packageName != null && packageName.length() > 0)
		{
			builder.append(packageName).append(S_DOT);
		}

		builder.append(classBytecode.getSourceFile());

		return builder.toString();
	}

	public static void addSourceClassMapping(ClassBC classBytecode)
	{	
		String fqName = getFullyQualifiedSourceName(classBytecode);
	
		List<ClassBC> classBytecodeList = sourceToClassMap.get(fqName);

		if (classBytecodeList == null)
		{
			classBytecodeList = new ArrayList<>();

			sourceToClassMap.put(fqName, classBytecodeList);
		}

		classBytecodeList.add(classBytecode);
	}

	public static List<ClassBC> getClassBytecodeList(ClassBC classBytecode)
	{
		String fqName = getFullyQualifiedSourceName(classBytecode);

		List<ClassBC> result = sourceToClassMap.get(fqName);

		if (result == null)
		{
			result = new ArrayList<>();
		}

		return Collections.unmodifiableList(result);
	}

	public static MemberBytecode getMemberBytecodeForSourceLine(ClassBC classBytecode, int sourceLine)
	{		
		MemberBytecode result = null;

		String fqName = getFullyQualifiedSourceName(classBytecode);

		List<ClassBC> classBytecodeList = sourceToClassMap.get(fqName);

		if (classBytecodeList != null)
		{
			if (DEBUG_LOGGING_TRIVIEW)
			{
				logger.debug("Found {} ClassBC for source {}", classBytecodeList.size(), fqName);
			}

			outer: for (ClassBC classBC : classBytecodeList)
			{
				for (MemberBytecode tempMemberBytecode : classBC.getMemberBytecodeList())
				{
					LineTable lineTable = tempMemberBytecode.getLineTable();

					if (DEBUG_LOGGING_TRIVIEW)
					{
						logger.debug("Checking LineTable\n{}", lineTable.toString());
					}

					if (lineTable.sourceLineInRange(sourceLine))
					{
						result = tempMemberBytecode;
						break outer;
					}
				}
			}
		}
		else
		{
			logger.warn("No source-bytecode mapping found for class {}", classBytecode.getFullyQualifiedClassName());
		}

		if (DEBUG_LOGGING_TRIVIEW)
		{
			logger.debug("Found bytecode offset {}", result);
		}

		return result;
	}

	public static int getSourceLineFromBytecode(MemberBytecode memberBytecode, int bytecodeOffset)
	{
		int result = -1;

		if (memberBytecode != null)
		{
			LineTable lineTable = memberBytecode.getLineTable();
			
			result = lineTable.findSourceLineForBytecodeOffset(bytecodeOffset);
		}

		return result;
	}
}