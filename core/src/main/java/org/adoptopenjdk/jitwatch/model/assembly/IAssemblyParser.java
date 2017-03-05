/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

public interface IAssemblyParser
{
	AssemblyMethod parseAssembly(String assemblyString);
	
	AssemblyInstruction createInstruction(AssemblyLabels labels, final String inLine);
	
	AssemblyInstruction parseInstruction(String input, long address, String comment, String annotation,	AssemblyLabels labels);
	
	boolean isConstant(String mnemonic, String operand);

	boolean isRegister(String mnemonic, String operand);

	boolean isAddress(String mnemonic, String operand);

	boolean isJump(String mnemonic);

	String extractRegisterName(final String input);
}