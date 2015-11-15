/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import javafx.stage.Stage;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.ui.triview.ITriView;

public interface IStageAccessProxy
{
	ITriView openTriView(IMetaMember member, boolean force);
	
	ITriView openTriView(IMetaMember member, boolean force, double width, double height);
	
	void openBrowser(String title, String html, String stylesheet);
	
	void openTextViewer(String title, String contents, boolean lineNumbers, boolean highlighting);

	Stage getStageForDialog();
	
	JITWatchConfig getConfig();
}