package com.chrisnewland.jitwatch.model.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.util.ParseUtil;

public class ClassBC
{
	private ConstantPool constantPool;
	private String sourceFile;
	private String majorVersion;
	private String minorVersion;
	private Map<String, List<Instruction>> memberBytecodeMap = new HashMap<>();

	public void addMemberBytecode(String memberName, List<Instruction> instructions)
	{
		memberBytecodeMap.put(memberName, instructions);
	}
	
	public List<Instruction> getMemberBytecode(IMetaMember member)
	{
		String bytecodeSignature = member.getSignatureForBytecode();
		
		List<Instruction> result = getMemberBytecode(bytecodeSignature);

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
	
	public List<Instruction> getMemberBytecode(String memberName)
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

	public String getMajorVersion()
	{
		return majorVersion;
	}

	public void setMajorVersion(String majorVersion)
	{
		this.majorVersion = majorVersion;
	}

	public String getMinorVersion()
	{
		return minorVersion;
	}

	public void setMinorVersion(String minorVersion)
	{
		this.minorVersion = minorVersion;
	}

}