package com.chrisnewland.jitwatch.ui.sandbox;

import com.chrisnewland.jitwatch.model.IMetaMember;

public interface ISandboxStage
{
	public void log(String text);
	
	public void openTriView(IMetaMember member);
	
	public void showError(String error);
}