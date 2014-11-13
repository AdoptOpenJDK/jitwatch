/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import java.io.File;

import javafx.stage.Stage;

import org.adoptopenjdk.jitwatch.core.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.model.IMetaMember;

public interface ISandboxStage extends ILogParseErrorListener
{
	void openTriView(IMetaMember member);
	
	void showError(String error);
	
	void editorClosed(EditorPane editor);
		
	void runFile(EditorPane editor);
	
	void addSourceFolder(File dir);
	
	void setVMLanguageFromFileExtension(String extension);
	
	Stage getStageForChooser();
	
	void log(String msg);
}