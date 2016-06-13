/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import java.io.File;
import java.io.IOException;

import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.ParsedClasspath;
import org.adoptopenjdk.jitwatch.model.SplitLog;

public interface ILogParser
{
	void setConfig(JITWatchConfig config);
	
	void processLogFile(File hotspotLog, ILogParseErrorListener listener) throws IOException;
	
	SplitLog getSplitLog();
	
	void stopParsing();
	
	ParsedClasspath getParsedClasspath();
	
	JITDataModel getModel();
	
	JITWatchConfig getConfig();
	
	void reset();
		
	boolean hasParseError();
	
	boolean isTweakVMLog();
	
	String getVMCommand();
	
	void discardParsedLogs();
}