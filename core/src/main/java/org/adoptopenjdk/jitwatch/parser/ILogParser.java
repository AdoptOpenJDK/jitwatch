/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.ParsedClasspath;
import org.adoptopenjdk.jitwatch.model.SplitLog;

public interface ILogParser
{
	void setConfig(JITWatchConfig config);

	default void processLogFile(File logFile, ILogParseErrorListener listener) throws IOException{
		processLogFile(new FileReader(logFile), listener);
	}

	void processLogFile(Reader logFileReader, ILogParseErrorListener listener) throws IOException;

	SplitLog getSplitLog();

	void stopParsing();

	ParsedClasspath getParsedClasspath();

	JITDataModel getModel();

	JITWatchConfig getConfig();

	void reset();

	boolean hasParseError();

	String getVMCommand();

	void discardParsedLogs();
}