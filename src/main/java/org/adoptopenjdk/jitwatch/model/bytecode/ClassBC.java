package org.adoptopenjdk.jitwatch.model.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.util.ParseUtil;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class ClassBC
{
	private ConstantPool constantPool;
	private String sourceFile;
	private int majorVersion;
	private int minorVersion;
	private Map<String, MemberBytecode> memberBytecodeMap = new HashMap<>();
	
	private LineTable compositeLineTable = null;

	public void putMemberBytecode(String memberName, MemberBytecode memberBytecode)
	{
		if (memberName == null)
		{
			throw new RuntimeException("tried to add null");
		}
		
		memberBytecodeMap.put(memberName, memberBytecode);
	}
	
	public LineTableEntry findLineTableEntryForSourceLine(int sourceLine)
	{		
		if (compositeLineTable == null)
		{
			buildCompositeLineTable();
		}

		return compositeLineTable.getEntryForSourceLine(sourceLine);
	}
	
	private void buildCompositeLineTable()
	{
		compositeLineTable = new LineTable();
		
		for (MemberBytecode memberBC : memberBytecodeMap.values())
		{
			LineTable lineTable = memberBC.getLineTable();
			
			compositeLineTable.add(lineTable);
		}		
		
		compositeLineTable.sort();
	}

	public MemberBytecode getMemberBytecode(IMetaMember member)
	{
		String bytecodeSignature = member.getSignatureForBytecode();

		MemberBytecode result = getMemberBytecode(bytecodeSignature);

		if (result == null)
		{
			List<String> keys = new ArrayList<>(getBytecodeMethodSignatures());

			bytecodeSignature = ParseUtil.findBestMatchForMemberSignature(member, keys);

			if (bytecodeSignature != null)
			{
				result = getMemberBytecode(bytecodeSignature);
			}
		}

		return result;
	}

	public MemberBytecode getMemberBytecode(String memberName)
	{
		return memberBytecodeMap.get(memberName);
	}

	public Set<String> getBytecodeMethodSignatures()
	{
		return memberBytecodeMap.keySet();
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

		for (Map.Entry<String, MemberBytecode> entry : memberBytecodeMap.entrySet())
		{
			builder.append("member: ").append(entry.getKey());
		}

		return builder.toString();
	}

}