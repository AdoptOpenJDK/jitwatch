/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import java.io.File;
import java.io.IOException;

import org.adoptopenjdk.jitwatch.model.JITDataModel;

public interface ILogParser
{
	void setConfig(JITWatchConfig config);
	
	void readLogFile(File hotspotLog) throws IOException;
	
	void stopParsing();
	
	JITDataModel getModel();
	
	JITWatchConfig getConfig();
	
	void reset();
	
	boolean hasTraceClassLoading();
}