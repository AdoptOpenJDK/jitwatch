/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan;

import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;

public interface IJarScanOperation
{
	void processInstructions(String className, MemberBytecode memberBytecode);
		
	String getReport();
}