package org.adoptopenjdk.jitwatch.ui.sandbox;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

public interface ISandboxStage
{
	void log(String text);
	
	void openTriView(IMetaMember member);
	
	void showError(String error);
}