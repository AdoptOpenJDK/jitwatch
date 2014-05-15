package com.chrisnewland.jitwatch.model.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberBytecode
{
	private Map<Integer, Integer> sourceToBytecodeMap = new HashMap<>();
	private List<BytecodeInstruction> bytecodeInstructions = new ArrayList<>();
	
	public MemberBytecode(List<BytecodeInstruction> bytecodeInstructions, Map<Integer, Integer> sourceToBytecodeMap)
	{
		this.bytecodeInstructions = bytecodeInstructions;
		this.sourceToBytecodeMap = sourceToBytecodeMap;
	}

	public List<BytecodeInstruction> getBytecodeInstructions()
	{
		return bytecodeInstructions;
	}

	public int getBytecodeForSource(int line)
	{
		int result = -1;
		
		if (sourceToBytecodeMap.containsKey(line))
		{
			result = sourceToBytecodeMap.get(line);
		}
		
		return result;
	}	
}