package org.adoptopenjdk.jitwatch.model.bytecode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;

public class SourceMapper
{
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

		outer: for (ClassBC classBC : classBytecodeList)
		{			
			for (MemberBytecode tempMemberBytecode : classBC.getMemberBytecodeList())
			{
				LineTable lineTable = tempMemberBytecode.getLineTable();

				if (lineTable.sourceLineInRange(sourceLine))
				{
					result = tempMemberBytecode;
					break outer;
				}
			}
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