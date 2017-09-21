package org.adoptopenjdk.jitwatch.ui.main;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

public interface ICompilationChangeListener
{
	void compilationChanged(IMetaMember member);
}