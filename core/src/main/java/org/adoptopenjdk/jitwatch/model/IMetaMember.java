/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;

public interface IMetaMember
{
	MetaClass getMetaClass();
	
	MemberBytecode getMemberBytecode();
	
	List<BytecodeInstruction> getInstructions();

	void addJournalEntry(Tag entry);
	Journal getJournal();

	String getQueuedAttribute(String key);
	void setQueuedAttributes(Map<String, String> queuedAttributes);
	boolean isQueued();

	void setCompiledAttributes(Map<String, String> compiledAttributes);
	void addCompiledAttributes(Map<String, String> additionalAttrs);	
	Map<String, String> getQueuedAttributes();
	Map<String, String> getCompiledAttributes();
	
	String getCompiledAttribute(String key);
	void addCompiledAttribute(String key, String value);
	boolean isCompiled();

	String toStringUnqualifiedMethodName(boolean fqParamTypes);

	String getMemberName();
	String getFullyQualifiedMemberName();
	String getAbbreviatedFullyQualifiedMemberName();

	int getModifier();
	String getModifierString();
	String getReturnTypeName();
	String[] getParamTypeNames();

	boolean matchesSignature(MemberSignatureParts msp, boolean matchTypesExactly);

	List<AssemblyMethod> getAssemblyMethods();

	void addAssembly(AssemblyMethod asmMethod);

	String getSignatureRegEx();
}