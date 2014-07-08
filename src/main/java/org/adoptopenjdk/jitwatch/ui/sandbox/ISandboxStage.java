package org.adoptopenjdk.jitwatch.ui.sandbox;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

public interface ISandboxStage
{
	public void log(String text);
	
	public void openTriView(IMetaMember member);
	
	public void showError(String error);
}