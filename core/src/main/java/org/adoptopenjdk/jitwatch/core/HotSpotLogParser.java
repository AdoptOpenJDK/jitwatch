/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NMSIZE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_STAMP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_STAMP_COMPLETED;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_FREE_CODE_CACHE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C1;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C2;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C2N;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_AT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_SQUARE_BRACKET;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_QUOTE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.LOADED;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.SKIP_BODY_TAGS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.SKIP_HEADER_TAGS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_AT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_FILE_COLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CLOSE_CDATA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CODE_CACHE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CODE_CACHE_FULL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_COMMAND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_HOTSPOT_LOG_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OPEN_CDATA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OPEN_CLOSE_CDATA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_RELEASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_START_COMPILE_THREAD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_SWEEPER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_QUEUED;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TWEAK_VM;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_VM_ARGUMENTS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_VM_VERSION;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_XML;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adoptopenjdk.jitwatch.model.CodeCacheEvent;
import org.adoptopenjdk.jitwatch.model.EventType;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.NumberedLine;
import org.adoptopenjdk.jitwatch.model.ParsedClasspath;
import org.adoptopenjdk.jitwatch.model.SplitLog;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.model.CodeCacheEvent.CodeCacheEventType;
import org.adoptopenjdk.jitwatch.util.ClassUtil;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotSpotLogParser implements ILogParser
{
	private static final Logger logger = LoggerFactory.getLogger(HotSpotLogParser.class);

	private JITDataModel model;

	private String vmCommand = null;

	private boolean isTweakVMLog = false;

	private boolean reading = false;

	boolean hasTraceClassLoad = false;

	private boolean hasParseError = false;
	private String errorDialogTitle;
	private String errorDialogBody;

	private IMetaMember currentMember = null;

	private IJITListener logListener = null;
	private ILogParseErrorListener errorListener = null;

	private boolean inHeader = false;

	private long parseLineNumber;
	private long processLineNumber;

	private JITWatchConfig config = new JITWatchConfig();

	private TagProcessor tagProcessor;

	private AssemblyProcessor asmProcessor;

	private SplitLog splitLog = new SplitLog();

	public HotSpotLogParser(IJITListener logListener)
	{
		model = new JITDataModel();

		this.logListener = logListener;
	}

	@Override
	public void setConfig(JITWatchConfig config)
	{
		this.config = config;
	}

	@Override
	public JITWatchConfig getConfig()
	{
		return config;
	}

	@Override
	public SplitLog getSplitLog()
	{
		return splitLog;
	}

	@Override
	public ParsedClasspath getParsedClasspath()
	{
		return config.getParsedClasspath();
	}

	private void configureDisposableClassLoader()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("configureDisposableClassLoader()");
		}

		List<String> configuredClassLocations = config.getConfiguredClassLocations();
		List<String> parsedClassLocations = getParsedClasspath().getClassLocations();

		int configuredClasspathCount = configuredClassLocations.size();
		int parsedClasspathCount = parsedClassLocations.size();

		List<URL> classpathURLList = new ArrayList<URL>(configuredClasspathCount + parsedClasspathCount);

		for (String filename : configuredClassLocations)
		{
			URI uri = new File(filename).toURI();

			logListener.handleLogEntry("Adding configured classpath: " + uri.toString());

			try
			{
				classpathURLList.add(uri.toURL());
			}
			catch (MalformedURLException e)
			{
				logger.error("Could not create URL: {} ", uri, e);
			}
		}

		for (String filename : getParsedClasspath().getClassLocations())
		{
			if (!configuredClassLocations.contains(filename))
			{
				URI uri = new File(filename).toURI();

				logListener.handleLogEntry("Adding parsed classpath: " + uri.toString());

				try
				{
					classpathURLList.add(uri.toURL());
				}
				catch (MalformedURLException e)
				{
					logger.error("Could not create URL: {} ", uri, e);
				}
			}
		}

		ClassUtil.initialise(classpathURLList);
	}

	private void logEvent(JITEvent event)
	{
		if (logListener != null)
		{
			logListener.handleJITEvent(event);
		}
	}

	private void logError(String entry)
	{
		if (logListener != null)
		{
			logListener.handleErrorEntry(entry);
		}
	}

	@Override
	public JITDataModel getModel()
	{
		return model;
	}

	@Override
	public void reset()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("HotSpotLogParser.reset()");
		}

		ClassUtil.clear();

		getModel().reset();

		splitLog.clear();

		hasTraceClassLoad = false;

		isTweakVMLog = false;

		hasParseError = false;
		errorDialogTitle = null;
		errorDialogBody = null;

		reading = false;

		inHeader = false;

		currentMember = null;

		vmCommand = null;

		parseLineNumber = 0;
		processLineNumber = 0;

		tagProcessor = new TagProcessor();

		asmProcessor = new AssemblyProcessor();
	}

	@Override
	public void processLogFile(File hotspotLog, ILogParseErrorListener errorListener)
	{
		reset();

		// tell listener to reset any data
		logListener.handleReadStart();
		
		this.errorListener = errorListener;

		splitLogFile(hotspotLog);

		if (DEBUG_LOGGING)
		{
			logSplitStats();
		}

		parseLogFile();
	}

	@Override
	public void discardParsedLogs()
	{
		splitLog.clear();
		splitLog = new SplitLog();
	}

	private void parseLogFile()
	{
		parseHeaderLines();

		buildParsedClasspath();

		configureDisposableClassLoader();

		buildClassModel();

		parseLogCompilationLines();

		parseAssemblyLines();

		checkIfErrorDialogNeeded();

		logListener.handleReadComplete();
	}

	private void checkIfErrorDialogNeeded()
	{
		if (!hasParseError)
		{
			if (!hasTraceClassLoad)
			{
				hasParseError = true;

				errorDialogTitle = "Missing VM Switch -XX:+TraceClassLoading";
				errorDialogBody = "JITWatch requires the -XX:+TraceClassLoading VM switch to be used.\nPlease recreate your log file with this switch enabled.";
			}
		}

		if (hasParseError)
		{
			errorListener.handleError(errorDialogTitle, errorDialogBody);
		}
	}

	private void parseHeaderLines()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("parseHeaderLines()");
		}

		for (NumberedLine numberedLine : splitLog.getHeaderLines())
		{
			if (!skipLine(numberedLine.getLine(), SKIP_HEADER_TAGS))
			{
				Tag tag = tagProcessor.processLine(numberedLine.getLine());

				processLineNumber = numberedLine.getLineNumber();

				if (tag != null)
				{
					handleTag(tag);
				}
			}
		}
	}

	private void parseLogCompilationLines()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("parseLogCompilationLines()");
		}

		for (NumberedLine numberedLine : splitLog.getLogCompilationLines())
		{
			if (!skipLine(numberedLine.getLine(), SKIP_BODY_TAGS))
			{
				Tag tag = tagProcessor.processLine(numberedLine.getLine());

				processLineNumber = numberedLine.getLineNumber();

				if (tag != null)
				{
					handleTag(tag);
				}
			}
		}
	}

	private void parseAssemblyLines()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("parseAssemblyLines()");
		}

		for (NumberedLine numberedLine : splitLog.getAssemblyLines())
		{
			processLineNumber = numberedLine.getLineNumber();

			asmProcessor.handleLine(numberedLine.getLine());
		}

		asmProcessor.complete();

		asmProcessor.attachAssemblyToMembers(model.getPackageManager());

		asmProcessor.clear();
	}

	private void splitLogFile(File hotspotLog)
	{
		reading = true;

		BufferedReader reader = null;

		try
		{
			reader = new BufferedReader(new FileReader(hotspotLog), 65536);

			String currentLine = reader.readLine();

			while (reading && currentLine != null)
			{
				try
				{
					String trimmedLine = currentLine.trim();

					if (trimmedLine.length() > 0)
					{
						char firstChar = trimmedLine.charAt(0);

						if (firstChar == C_OPEN_ANGLE || firstChar == C_OPEN_SQUARE_BRACKET || firstChar == C_AT)
						{
							currentLine = trimmedLine;
						}

						handleLogLine(currentLine);
					}
				}
				catch (Exception ex)
				{
					logger.error("Exception handling: '{}'", currentLine, ex);
				}

				currentLine = reader.readLine();
			}
		}
		catch (IOException ioe)
		{
			logger.error("Exception while splitting log file", ioe);
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Exception e)
				{
					logger.error("Could not close reader");
				}
			}
		}
	}

	private void logSplitStats()
	{
		logger.debug("Header lines        : {}", splitLog.getHeaderLines().size());
		logger.debug("ClassLoader lines   : {}", splitLog.getClassLoaderLines().size());
		logger.debug("LogCompilation lines: {}", splitLog.getLogCompilationLines().size());
		logger.debug("Assembly lines      : {}", splitLog.getAssemblyLines().size());
	}

	@Override
	public void stopParsing()
	{
		reading = false;
	}

	private boolean skipLine(final String line, final Set<String> skipSet)
	{
		boolean isSkip = false;

		for (String skip : skipSet)
		{
			if (line.startsWith(skip))
			{
				isSkip = true;
				break;
			}
		}

		return isSkip;
	}

	private void handleLogLine(final String inCurrentLine)
	{
		String currentLine = inCurrentLine;

		NumberedLine numberedLine = new NumberedLine(parseLineNumber++, currentLine);

		if (TAG_TTY.equals(currentLine))
		{
			inHeader = false;
			return;
		}
		else if (currentLine.startsWith(TAG_XML))
		{
			inHeader = true;
		}

		if (inHeader)
		{
			// HotSpot log header XML can have text nodes so consume all lines
			splitLog.addHeaderLine(numberedLine);
		}
		else
		{
			if (currentLine.startsWith(TAG_OPEN_CDATA) || currentLine.startsWith(TAG_CLOSE_CDATA)
					|| currentLine.startsWith(TAG_OPEN_CLOSE_CDATA))
			{
				// ignore, TagProcessor will recognise from <fragment> tag
			}
			else if (currentLine.startsWith(S_OPEN_ANGLE))
			{
				// After the header, XML nodes do not have text nodes
				splitLog.addLogCompilationLine(numberedLine);
			}
			else if (currentLine.startsWith(LOADED))
			{
				splitLog.addClassLoaderLine(numberedLine);
			}
			else if (currentLine.startsWith(S_AT))
			{
				// possible PrintCompilation was enabled as well as
				// LogCompilation?
				// jmh does this with perf annotations
				// Ignore this line
			}
			else
			{
				// need to cope with nmethod appearing on same line as last hlt
				// 0x0000 hlt <nmethod compile_id= ....

				int indexNMethod = currentLine.indexOf(S_OPEN_ANGLE + TAG_NMETHOD);

				if (indexNMethod != -1)
				{
					if (DEBUG_LOGGING)
					{
						logger.debug("detected nmethod tag mangled with assembly");
					}

					String assembly = currentLine.substring(0, indexNMethod);

					String remainder = currentLine.substring(indexNMethod);

					numberedLine.setLine(assembly);

					splitLog.addAssemblyLine(numberedLine);

					handleLogLine(remainder);
				}
				else
				{
					splitLog.addAssemblyLine(numberedLine);
				}
			}
		}
	}

	private void handleTag(Tag tag)
	{
		String tagName = tag.getName();

		switch (tagName)
		{
		case TAG_VM_VERSION:
			handleVmVersion(tag);
			break;

		case TAG_TASK_QUEUED:
			handleTagQueued(tag);
			break;

		case TAG_NMETHOD:
			handleTagNMethod(tag);
			break;

		case TAG_TASK:
			handleTagTask((Task)tag);
			break;

		case TAG_SWEEPER:
			storeCodeCacheEvent(CodeCacheEventType.SWEEPER, tag);
			break;
			
		case TAG_CODE_CACHE_FULL:
			storeCodeCacheEvent(CodeCacheEventType.CACHE_FULL, tag);
			break;

		case TAG_HOTSPOT_LOG_DONE:
			model.setEndOfLog(tag);
			break;

		case TAG_START_COMPILE_THREAD:
			handleStartCompileThread(tag);
			break;

		case TAG_VM_ARGUMENTS:
			handleTagVmArguments(tag);
			break;

		default:
			break;
		}
	}

	private void handleVmVersion(Tag tag)
	{
		String release = tag.getNamedChildren(TAG_RELEASE).get(0).getTextContent();

		model.setVmVersionRelease(release);

		List<Tag> tweakVMTags = tag.getNamedChildren(TAG_TWEAK_VM);

		if (tweakVMTags.size() == 1)
		{
			isTweakVMLog = true;

			if (DEBUG_LOGGING)
			{
				logger.debug("TweakVM detected!");
			}
		}
	}

	private void handleTagVmArguments(Tag tag)
	{
		List<Tag> tagCommandChildren = tag.getNamedChildren(TAG_COMMAND);

		if (tagCommandChildren.size() > 0)
		{
			vmCommand = tagCommandChildren.get(0).getTextContent();

			if (DEBUG_LOGGING)
			{
				logger.debug("VM Command: {}", vmCommand);
			}
		}
	}

	private void handleStartCompileThread(Tag tag)
	{
		model.getJITStats().incCompilerThreads();
	}

	public IMetaMember findMemberWithSignature(String logSignature)
	{
		IMetaMember result = null;

		try
		{
			result = ParseUtil.findMemberWithSignature(model, logSignature);
		}
		catch (LogParseException ex)
		{
			if (DEBUG_LOGGING)
			{
				logger.debug("Could not parse signature: {}", logSignature);
				logger.debug("Exception was {}", ex.getMessage());
			}

			logError("Could not parse line " + processLineNumber + " : " + logSignature + " : " + ex.getMessage());
		}

		return result;
	}

	private void handleTagQueued(Tag tag)
	{
		handleMethodLine(tag, EventType.QUEUE);
	}

	private void renameCompilationCompletedTimestamp(Tag tag)
	{
		String compilationCompletedStamp = tag.getAttributes().get(ATTR_STAMP);

		if (compilationCompletedStamp != null)
		{
			tag.getAttributes().remove(ATTR_STAMP);
			tag.getAttributes().put(ATTR_STAMP_COMPLETED, compilationCompletedStamp);
		}
	}

	private void handleTagNMethod(Tag tag)
	{
		Map<String, String> tagAttributes = tag.getAttributes();

		String attrCompiler = tagAttributes.get(ATTR_COMPILER);

		renameCompilationCompletedTimestamp(tag);

		if (attrCompiler != null)
		{
			if (C1.equals(attrCompiler))
			{
				handleMethodLine(tag, EventType.NMETHOD_C1);
			}
			else if (C2.equals(attrCompiler))
			{
				handleMethodLine(tag, EventType.NMETHOD_C2);
			}
			else
			{
				logError("Unexpected Compiler attribute: " + attrCompiler);
			}
		}
		else
		{
			String attrCompileKind = tagAttributes.get(ATTR_COMPILE_KIND);

			if (attrCompileKind != null && C2N.equals(attrCompileKind))
			{
				handleMethodLine(tag, EventType.NMETHOD_C2N);
			}
			else
			{
				logError("Missing Compiler attribute " + tag);
			}
		}
	}

	private void handleTagTask(Task task)
	{
		handleMethodLine(task, EventType.TASK);

		Tag tagCodeCache = task.getFirstNamedChild(TAG_CODE_CACHE);
		Tag tagTaskDone = task.getFirstNamedChild(TAG_TASK_DONE);
				
		if (tagTaskDone != null)
		{
			handleTaskDone(tagTaskDone);
			
			if (tagCodeCache != null)
			{
				long stamp = ParseUtil.parseStampFromTag(tagTaskDone);
				long freeCodeCache = ParseUtil.parseLongAttributeFromTag(tagCodeCache, ATTR_FREE_CODE_CACHE);
				long nativeCodeSize = ParseUtil.parseLongAttributeFromTag(tagTaskDone, ATTR_NMSIZE);
				
				storeCodeCacheEventDetail(CodeCacheEventType.COMPILATION, stamp, nativeCodeSize, freeCodeCache);
			}
		}
		else
		{
			logger.error("{} not found in {}", TAG_TASK_DONE, task);
		}
	}

	private void storeCodeCacheEvent(CodeCacheEventType eventType, Tag tag)
	{
		storeCodeCacheEventDetail(eventType, ParseUtil.parseStampFromTag(tag), 0, 0);
	}

	private void storeCodeCacheEventDetail(CodeCacheEventType eventType, long stamp, long nativeCodeSize, long freeCodeCache)
	{
		CodeCacheEvent codeCacheEvent = new CodeCacheEvent(eventType, stamp, nativeCodeSize, freeCodeCache);

		model.addCodeCacheEvent(codeCacheEvent);
	}

	private void handleMethodLine(Tag tag, EventType eventType)
	{
		Map<String, String> attrs = tag.getAttributes();

		String attrMethod = attrs.get(ATTR_METHOD);

		if (attrMethod != null)
		{
			attrMethod = attrMethod.replace(S_SLASH, S_DOT);

			handleMember(attrMethod, attrs, eventType, tag);
		}
	}

	private void handleMember(String signature, Map<String, String> attrs, EventType type, Tag tag)
	{
		IMetaMember metaMember = findMemberWithSignature(signature);

		long stampTime = ParseUtil.getStamp(attrs);

		if (metaMember != null)
		{
			switch (type)
			{
			case QUEUE:
			{
				metaMember.setTagTaskQueued(tag);
				JITEvent queuedEvent = new JITEvent(stampTime, type, metaMember);
				model.addEvent(queuedEvent);
				logEvent(queuedEvent);
			}
				break;
			case NMETHOD_C1:
			case NMETHOD_C2:
			case NMETHOD_C2N:
			{
				metaMember.setTagNMethod(tag);
				metaMember.getMetaClass().incCompiledMethodCount();
				model.updateStats(metaMember, attrs);

				JITEvent compiledEvent = new JITEvent(stampTime, type, metaMember);
				model.addEvent(compiledEvent);
				logEvent(compiledEvent);
			}
				break;
			case TASK:
			{
				metaMember.setTagTask((Task) tag);
				currentMember = metaMember;
			}
				break;
			}
		}
	}

	private void handleTaskDone(Tag tagTaskDone)
	{
		Map<String, String> attrs = tagTaskDone.getAttributes();

		if (attrs.containsKey(ATTR_NMSIZE))
		{
			long nmsize = Long.parseLong(attrs.get(ATTR_NMSIZE));
			model.addNativeBytes(nmsize);
		}

		if (currentMember != null)
		{
			Tag parent = tagTaskDone.getParent();

			String compileID = null;

			if (TAG_TASK.equals(parent.getName()))
			{
				compileID = parent.getAttributes().get(ATTR_COMPILE_ID);

				if (compileID != null)
				{
					currentMember.setTagTaskDone(compileID, tagTaskDone);
				}
				else
				{
					logger.warn("No compile_id attribute found on task");
				}
			}
			else
			{
				logger.warn("Unexpected parent of task_done: {}", parent.getName());
			}

			// prevents attr overwrite by next task_done if next member not
			// found due to classpath issues
			currentMember = null;
		}
	}

	private void buildParsedClasspath()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("buildParsedClasspath()");
		}

		for (NumberedLine numberedLine : splitLog.getClassLoaderLines())
		{
			buildParsedClasspath(numberedLine.getLine());
		}
	}

	private void buildClassModel()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("buildClassModel()");
		}

		for (NumberedLine numberedLine : splitLog.getClassLoaderLines())
		{
			buildClassModel(numberedLine.getLine());
		}
	}

	private void buildParsedClasspath(String inCurrentLine)
	{
		if (!hasTraceClassLoad)
		{
			hasTraceClassLoad = true;
		}

		final String FROM_SPACE = "from ";

		String originalLocation = null;

		int fromSpacePos = inCurrentLine.indexOf(FROM_SPACE);

		if (fromSpacePos != -1)
		{
			originalLocation = inCurrentLine.substring(fromSpacePos + FROM_SPACE.length(), inCurrentLine.length() - 1);
		}

		if (originalLocation != null && originalLocation.startsWith(S_FILE_COLON))
		{
			originalLocation = originalLocation.substring(S_FILE_COLON.length());

			try
			{
				originalLocation = URLDecoder.decode(originalLocation, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				// ignore
			}

			getParsedClasspath().addClassLocation(originalLocation);
		}
	}

	private void buildClassModel(String inCurrentLine)
	{
		String fqClassName = StringUtil.getSubstringBetween(inCurrentLine, LOADED, S_SPACE);

		if (fqClassName != null)
		{
			addToClassModel(fqClassName);
		}
	}

	private void addToClassModel(String fqClassName)
	{
		Class<?> clazz = null;

		MetaClass metaClass = model.getPackageManager().getMetaClass(fqClassName);

		if (metaClass != null)
		{
			return;
		}

		try
		{
			clazz = ClassUtil.loadClassWithoutInitialising(fqClassName);

			if (clazz != null)
			{
				model.buildAndGetMetaClass(clazz);
			}
		}
		catch (ClassNotFoundException cnf)
		{
			if (!ParseUtil.possibleLambdaMethod(fqClassName))
			{
				logError("ClassNotFoundException: '" + fqClassName + C_QUOTE);
			}
		}
		catch (NoClassDefFoundError ncdf)
		{
			logError("NoClassDefFoundError: '" + fqClassName + C_SPACE + "requires " + ncdf.getMessage() + C_QUOTE);
		}
		catch (UnsupportedClassVersionError ucve)
		{
			hasParseError = true;
			errorDialogTitle = "UnsupportedClassVersionError for class " + fqClassName;
			errorDialogBody = "Could not load " + fqClassName + " as the class file version is too recent for this JVM.";

			logError(
					"UnsupportedClassVersionError! Tried to load a class file with an unsupported format (later version than this JVM)");
			logger.error("Class file for {} created in a later JVM version", fqClassName, ucve);
		}
		catch (Throwable t)
		{
			// Possibly a VerifyError
			logger.error("Could not addClassToModel {}", fqClassName, t);
			logError("Exception: '" + fqClassName + C_QUOTE);
		}
	}

	@Override
	public boolean hasParseError()
	{
		return hasParseError;
	}

	@Override
	public boolean isTweakVMLog()
	{
		return isTweakVMLog;
	}

	@Override
	public String getVMCommand()
	{
		return vmCommand;
	}

}
