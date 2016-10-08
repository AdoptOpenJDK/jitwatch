/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;

public interface IParseDictionary
{
	void putType(String id, Tag type);

	void putKlass(String id, Tag klass);

	void putMethod(String id, Tag method);
	
	void putBCIOpcode(String methodID, int bci, Opcode opcode);

	Tag getType(String id);

	Tag getKlass(String id);

	Tag getMethod(String id);
	
	String getParseMethod();
	
	BCIOpcodeMap getBCIOpcodeMap(String methodID);
}