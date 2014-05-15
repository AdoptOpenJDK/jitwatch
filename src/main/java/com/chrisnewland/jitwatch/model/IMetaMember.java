/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.model.assembly.AssemblyMethod;
import com.chrisnewland.jitwatch.model.bytecode.MemberBytecode;

public interface IMetaMember
{
    //===================================
    // This is dumb. Use modifier class
    //===================================
	String PUBLIC = "public";
	String PRIVATE = "private";
	String PROTECTED = "protected";
	String STATIC = "static";
	String FINAL = "final";
	String SYNCHRONIZED = "synchronized";
	String STRICTFP = "strictfp";
	String NATIVE = "native";
	String ABSTRACT = "abstract";

	String[] MODIFIERS = new String[] { PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, SYNCHRONIZED, STRICTFP, NATIVE, ABSTRACT };
	//===================================

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

	String toStringUnqualifiedMethodName();
	
	String getMemberName();
	String getFullyQualifiedMemberName();

	int getModifier();
	String getModifierString();
	String getReturnTypeName();
	String[] getParamTypeNames();

	boolean matches(String input);

	AssemblyMethod getAssembly();

	void setAssembly(AssemblyMethod asmMethod);

	String getSignatureRegEx();

	String getSignatureForBytecode();
	
	List<String> getTreePath();
	
	MemberBytecode getBytecodeForMember(List<String> classLocations);
}