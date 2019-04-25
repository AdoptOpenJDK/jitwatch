/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.main;

import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.model.IMetaMember;

public interface IMemberSelectedListener
{
	void selectMember(IMetaMember member, boolean updateTree, boolean updateTriView);
	
	void selectCompilation(IMetaMember member, int compilationIndex);

	void selectCompileNode(CompileNode compileNode);
}