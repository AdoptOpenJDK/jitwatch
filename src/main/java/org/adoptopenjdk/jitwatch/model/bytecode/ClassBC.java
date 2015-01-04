/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_BYTECODE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_BYTECODE_MAJOR_VERSION;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_BYTECODE_MINOR_VERSION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassBC
{
	private ConstantPool constantPool;
	private String sourceFile;
	private int majorVersion;
	private int minorVersion;
	private List<MemberBytecode> memberBytecodeList = new ArrayList<>();

	private Map<String, String> classGenericsMap = new LinkedHashMap<>();
	
	private LineTable compositeLineTable = null;

	private static final Logger logger = LoggerFactory.getLogger(ClassBC.class);

	public void addMemberBytecode(MemberBytecode memberBytecode)
	{
		memberBytecodeList.add(memberBytecode);
	}

	public List<MemberBytecode> getMemberBytecodeList()
	{
		return Collections.unmodifiableList(memberBytecodeList);
	}

	public MemberBytecode getMemberBytecodeForSourceLine(int sourceLine)
	{
		MemberBytecode result = null;

		for (MemberBytecode memberBytecode : memberBytecodeList)
		{
			if (DEBUG_LOGGING_BYTECODE)
			{
				logger.debug("checking lineTable for member {}", memberBytecode.getMemberSignatureParts().getMemberName());
			}

			LineTable lineTable = memberBytecode.getLineTable();

			if (lineTable.sourceLineInRange(sourceLine))
			{
				result = memberBytecode;
				break;
			}
		}

		return result;
	}

	public LineTableEntry findLineTableEntryForSourceLine(int sourceLine)
	{
		if (compositeLineTable == null)
		{
			buildCompositeLineTable();
		}

		return compositeLineTable.getEntryForSourceLine(sourceLine);
	}
	
	public void addGenericsMapping(String key, String value)
	{
		classGenericsMap.put(key, value);
	}

	public Map<String, String> getGenericsMap()
	{
		return Collections.unmodifiableMap(classGenericsMap);
	}
	
	private void buildCompositeLineTable()
	{
		compositeLineTable = new LineTable();

		for (MemberBytecode memberBytecode : memberBytecodeList)
		{
			LineTable lineTable = memberBytecode.getLineTable();

			compositeLineTable.add(lineTable);
		}
	}

	public MemberBytecode getMemberBytecode(IMetaMember member)
	{
		if (DEBUG_LOGGING_BYTECODE)
		{
			logger.debug("getMemberBytecode: {}", member);
		}

		MemberBytecode result = null;

		if (member != null)
		{
			for (MemberBytecode item : memberBytecodeList)
			{
				if (member.matchesSignature(item.getMemberSignatureParts(), true))
				{
					result = item;
					break;
				}
			}
		}

		if (DEBUG_LOGGING_BYTECODE)
		{
			logger.debug("getMemberBytecode found: {}", result);
		}

		return result;
	}

	public MemberBytecode getMemberBytecodeForSignature(MemberSignatureParts msp)
	{
		if (DEBUG_LOGGING_BYTECODE)
		{
			logger.debug("getMemberBytecodeForSignature: {}", msp);
		}

		MemberBytecode result = null;

		if (msp != null)
		{
			for (MemberBytecode item : memberBytecodeList)
			{
				if (msp.equals(item.getMemberSignatureParts()))
				{
					result = item;
					break;
				}
			}
		}

		if (DEBUG_LOGGING_BYTECODE)
		{
			logger.debug("Found MemberBytecode: {}", result != null);
		}

		return result;
	}

	public ConstantPool getConstantPool()
	{
		return constantPool;
	}

	public void setConstantPool(ConstantPool constantPool)
	{
		this.constantPool = constantPool;
	}

	public String getSourceFile()
	{
		return sourceFile;
	}

	public void setSourceFile(String sourceFile)
	{
		this.sourceFile = sourceFile;
	}

	public int getMajorVersion()
	{
		return majorVersion;
	}

	public void setMajorVersion(int majorVersion)
	{
		this.majorVersion = majorVersion;
	}

	public int getMinorVersion()
	{
		return minorVersion;
	}

	public String getJavaVersion()
	{
		String result;

		switch (majorVersion)
		{
		case 52:
			result = "Java 8";
			break;
		case 51:
			result = "Java 7";
			break;
		case 50:
			result = "Java 6.0";
			break;
		case 49:
			result = "Java 5.0";
			break;
		case 48:
			result = "Java 1.4";
			break;
		case 47:
			result = "Java 1.3";
			break;
		case 46:
			result = "Java 1.2";
			break;
		case 45:
			result = "Java 1.1";
			break;
		default:
			result = "Unknown";
			break;
		}

		return result;
	}

	public void setMinorVersion(int minorVersion)
	{
		this.minorVersion = minorVersion;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(S_BYTECODE_MAJOR_VERSION).append(majorVersion).append(C_NEWLINE);
		builder.append(S_BYTECODE_MINOR_VERSION).append(minorVersion).append(C_NEWLINE);

		for (MemberBytecode item : memberBytecodeList)
		{
			builder.append("member: ").append(item).append(C_NEWLINE);
		}

		return builder.toString();
	}
}