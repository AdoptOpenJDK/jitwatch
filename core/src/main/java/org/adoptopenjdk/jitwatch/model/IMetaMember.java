/*
 * Copyright (c) 2013-2017 Chris Newland.
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

	String toStringUnqualifiedMethodName(boolean visibilityAndReturnType, boolean fqParamTypes);

	String getMemberName();
	String getFullyQualifiedMemberName();
	String getAbbreviatedFullyQualifiedMemberName();

	int getModifier();
	String getModifierString();
	String getReturnTypeName();
	String[] getParamTypeNames();

	boolean matchesSignature(MemberSignatureParts msp, boolean matchTypesExactly);

	boolean isConstructor();
	
	String getQueuedAttribute(String key);
	Map<String, String> getQueuedAttributes();
	
	void setTagTaskQueued(Tag tagTaskQueued);
	void setTagNMethod(Tag tagNMethod);
	void setTagTask(Task tagTask);
	void setTagTaskDone(String compileID, Tag tagTaskDone);
	
	String getCompiledAttribute(String key);
	Map<String, String> getCompiledAttributes();

	Compilation getCompilationByCompileID(String compileID);
	Compilation getCompilationByNativeAddress(String address);
	
	boolean isCompiled();
	
	void addAssembly(AssemblyMethod asmMethod);
	
	void setSelectedCompilation(int index);
	Compilation getSelectedCompilation();
	List<Compilation> getCompilations();
	Compilation getCompilation(int index);
	Compilation getLastCompilation();
	
	String getSourceMethodSignatureRegEx();
}