/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;

public interface IMetaMember
{
	List<String> getQueuedAttributes();

	MetaClass getMetaClass();
	
	void addJournalEntry(Tag entry);
	Journal getJournal();

	String getQueuedAttribute(String key);

	List<String> getCompiledAttributes();

	String getCompiledAttribute(String key);

	void addCompiledAttribute(String key, String value);

	void setQueuedAttributes(Map<String, String> queuedAttributes);

	boolean isQueued();

	void setCompiledAttributes(Map<String, String> compiledAttributes);

	void addCompiledAttributes(Map<String, String> additionalAttrs);

	boolean isCompiled();

	String toStringUnqualifiedMethodName(boolean fqParamTypes);
		
	String getMemberName();
	String getFullyQualifiedMemberName();
	String getAbbreviatedFullyQualifiedMemberName();

	int getModifier();
	String getModifierString();
	String getReturnTypeName();
	String[] getParamTypeNames();
	
	boolean matchesSignature(MemberSignatureParts msp);

	AssemblyMethod getAssembly();

	void setAssembly(AssemblyMethod asmMethod);

	String getSignatureRegEx();

	String getSignatureForBytecode();
}