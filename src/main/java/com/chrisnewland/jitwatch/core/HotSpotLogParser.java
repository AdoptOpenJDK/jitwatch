/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.JITDataModel;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.util.ClassUtil;
import com.chrisnewland.jitwatch.util.ParseUtil;
import com.chrisnewland.jitwatch.util.StringUtil;

public class HotSpotLogParser
{
	enum EventType
	{
		QUEUE, NMETHOD, TASK
	}

	private JITDataModel model;

	private boolean watching = false;

	private boolean inNativeCode = false;

	private StringBuilder nativeCodeBuilder = new StringBuilder();

	private IMetaMember currentMember = null;

	private IJITListener logListener = null;

	private long currentLineNumber;

	private JITWatchConfig config;

	private TagProcessor tagProcessor;

	public HotSpotLogParser(JITDataModel model, JITWatchConfig config, IJITListener logListener)
	{
		this.model = model;

		this.logListener = logListener;

		this.config = config;
	}

	private void mountAdditionalClasses()
	{
		for (String filename : config.getClassLocations())
		{
			URI uri = new File(filename).toURI();

			logListener.handleLogEntry("Adding classpath: " + uri.toString());

			ClassUtil.addURIToClasspath(uri);
		}
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

	public void watch(File hotspotLog) throws IOException
	{
		mountAdditionalClasses();

		currentLineNumber = 0;

		BufferedReader input = new BufferedReader(new FileReader(hotspotLog));

		String currentLine = null;

		tagProcessor = new TagProcessor();

		watching = true;

		while (watching)
		{
			if (currentLine != null)
			{
				try
				{
					handleLine(currentLine);
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}
			}
			else
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					break;
				}
			}

			currentLine = input.readLine();
		}

		input.close();
	}

	public void stop()
	{
		watching = false;
	}

	// <?xml version='1.0' encoding='UTF-8'?>
	// <hotspot_log version='160 1' process='20929' time_ms='1380789730403'>
	// <vm_version>
	// <name>
	// </name>
	// <release>
	// </release>
	// <info>
	// </info>
	// </vm_version>
	// <vm_arguments>
	// <args>
	// </args>
	// <command>
	// </command>
	// <launcher>
	// </launcher>
	// <properties>
	// </properties>
	// </vm_arguments>
	// <tty>
	// <task_queued /> ...
	// </tty>
	// <compilation_log>
	// <task > ...
	// <compilation_log>
	// <hotspot_log_done stamp='175.381'/>
	// </hotspot_log>

	private void handleLine(String currentLine)
	{
		currentLine = currentLine.replace("&apos;", "'");
		currentLine = currentLine.replace("&lt;", "<");
		currentLine = currentLine.replace("&gt;", ">");

		if (currentLine.startsWith("<"))
		{
			boolean isSkip = false;

			for (String skip : JITWatchConstants.SKIP_TAGS)
			{
				if (currentLine.startsWith(skip))
				{
					isSkip = true;
					break;
				}
			}

			if (!isSkip)
			{
				Tag tag = tagProcessor.processLine(currentLine);

				if (tag != null)
				{
					handleTag(tag);
				}
			}
		}
		else if (currentLine.startsWith(JITWatchConstants.LOADED))
		{
			if (inNativeCode)
			{
				completeNativeCode();
			}
			handleLoaded(currentLine);
		}
		else if (inNativeCode)
		{
			appendNativeCode(currentLine);
		}
		else if (currentLine.contains(JITWatchConstants.NATIVE_CODE_METHOD_MARK))
		{
			String sig = convertNativeCodeMethodName(currentLine);

			currentMember = findMemberWithSignature(sig);
			inNativeCode = true;

			appendNativeCode(currentLine);
		}
		else if (inNativeCode)
		{
			appendNativeCode(currentLine);
		}

		currentLineNumber++;
	}

	private void handleTag(Tag tag)
	{
		if (inNativeCode)
		{
			// TODO: file a bug report for mangled hotspot output
			completeNativeCode();
		}

		String tagName = tag.getName();

		switch (tagName)
		{
		case JITWatchConstants.TAG_TASK_QUEUED:
			handleMethodLine(tag, EventType.QUEUE);
			break;

		case JITWatchConstants.TAG_NMETHOD:
			handleMethodLine(tag, EventType.NMETHOD);
			break;

		case JITWatchConstants.TAG_TASK:
			handleMethodLine(tag, EventType.TASK);

			Tag tagCodeCache = tag.getFirstNamedChild(JITWatchConstants.TAG_CODE_CACHE);

			if (tagCodeCache != null)
			{
				// copy timestamp from parent <task> tag used for graphing code
				// cache
				String stamp = tag.getAttrs().get(JITWatchConstants.ATTR_STAMP);
				tagCodeCache.getAttrs().put(JITWatchConstants.ATTR_STAMP, stamp);

				model.addCodeCacheTag(tagCodeCache);
			}

			Tag tagTaskDone = tag.getFirstNamedChild(JITWatchConstants.TAG_TASK_DONE);

			if (tagTaskDone != null)
			{
				handleTaskDone(tagTaskDone);
			}

			break;

		case JITWatchConstants.TAG_START_COMPILE_THREAD:
			handleStartCompileThread();
			break;
		}

		Map<String, String> attrs = tag.getAttrs();

		String compileID = attrs.get(JITWatchConstants.ATTR_COMPILE_ID);
		String compileKind = attrs.get(JITWatchConstants.ATTR_COMPILE_KIND);

		String journalID;

		// osr compiles do not have unique compile IDs so concat compile_kind
		if (compileID != null && compileKind != null && JITWatchConstants.OSR.equals(compileKind))
		{
			journalID = compileID + compileKind;
		}
		else
		{
			journalID = compileID;
		}

		if (compileID != null)
		{
			model.addJournalEntry(journalID, tag);
		}
	}

	private void appendNativeCode(String line)
	{
		nativeCodeBuilder.append(line).append("\n");
	}

	private void completeNativeCode()
	{
		inNativeCode = false;

		if (currentMember != null)
		{
			currentMember.setAssembly(nativeCodeBuilder.toString());
		}

		nativeCodeBuilder.delete(0, nativeCodeBuilder.length());
	}

	private void handleStartCompileThread()
	{
		model.getJITStats().incCompilerThreads();
	}

	private void handleMethodLine(Tag tag, EventType eventType)
	{
		Map<String, String> attrs = tag.getAttrs();

		String fqMethodName = attrs.get(JITWatchConstants.METHOD);

		if (fqMethodName != null)
		{
			fqMethodName = fqMethodName.replace("/", ".");

			boolean packageOK = config.isAllowedPackage(fqMethodName);

			if (packageOK)
			{
				attrs.remove("method");
				handleMethod(fqMethodName, attrs, eventType);
			}
		}
	}

	private IMetaMember findMemberWithSignature(String logSignature)
	{
		IMetaMember metaMember = null;

		String[] parsedResult = null;

		try
		{
			parsedResult = ParseUtil.parseLogSignature(logSignature);
		}
		catch (Exception e)
		{
			logError(e.getMessage());
		}

		if (parsedResult != null)
		{
			String className = parsedResult[0];
			String parsedSignature = parsedResult[1];

			if (parsedSignature != null)
			{
				metaMember = model.findMetaMember(className, parsedSignature);
			}
		}
		else
		{
			logError("Could not parse line " + currentLineNumber + " : " + logSignature);
		}

		return metaMember;
	}

	private void handleMethod(String methodSignature, Map<String, String> attrs, EventType type)
	{
		IMetaMember metaMember = findMemberWithSignature(methodSignature);

		String stampAttr = attrs.get("stamp");
		long stampTime = ParseUtil.parseStamp(stampAttr);

		if (metaMember != null)
		{
			switch (type)
			{
			case QUEUE:
				metaMember.setQueuedAttributes(attrs);
				JITEvent queuedEvent = new JITEvent(stampTime, false, metaMember.toString());
				model.addEvent(queuedEvent);
				logEvent(queuedEvent);
				break;
			case NMETHOD:
				metaMember.setCompiledAttributes(attrs);
				metaMember.getMetaClass().incCompiledMethodCount();
				model.updateStats(metaMember);

				JITEvent compiledEvent = new JITEvent(stampTime, true, metaMember.toString());
				model.addEvent(compiledEvent);
				logEvent(compiledEvent);
				break;
			case TASK:
				metaMember.addCompiledAttributes(attrs);
				currentMember = metaMember;
				break;
			}
		}
	}

	private void handleTaskDone(Tag tag)
	{
		Map<String, String> attrs = tag.getAttrs();

		if (attrs.containsKey("nmsize"))
		{
			long nmsize = Long.parseLong(attrs.get("nmsize"));
			model.addNativeBytes(nmsize);
		}

		if (currentMember != null)
		{
			currentMember.addCompiledAttributes(attrs);

			// prevents attr overwrite by next task_done if next member not
			// found due to classpath issues
			currentMember = null;
		}
	}

	/*
	 * JITWatch needs classloader information so it can show classes which have
	 * no JIT-compiled methods in the class tree
	 */
	private void handleLoaded(String currentLine)
	{
		String fqClassName = StringUtil.getSubstringBetween(currentLine, JITWatchConstants.LOADED, " ");

		if (fqClassName != null)
		{
			String packageName;
			String className;

			int lastDotIndex = fqClassName.lastIndexOf('.');

			if (lastDotIndex != -1)
			{
				packageName = fqClassName.substring(0, lastDotIndex);
				className = fqClassName.substring(lastDotIndex + 1);
			}
			else
			{
				packageName = "";
				className = fqClassName;
			}

			boolean allowedPackage = config.isAllowedPackage(packageName);

			if (allowedPackage)
			{
				Class<?> clazz = null;

				try
				{
					clazz = ClassUtil.loadClassWithoutInitialising(fqClassName);
				}
				catch (ClassNotFoundException cnf)
				{
					logError("ClassNotFoundException: '" + fqClassName + "' parsing " + currentLine);
				}
				catch (NoClassDefFoundError ncdf)
				{
					logError("NoClassDefFoundError: '" + fqClassName + "' parsing " + currentLine);
				}

				try
				{
					// can throw NCDFE from clazz.getDeclaredMethods()
					model.buildMetaClass(packageName, className, clazz);
				}
				catch (NoClassDefFoundError ncdf)
				{
					// missing class is from a method declaration in fqClassName
					// so look in getMessage()
					logError("NoClassDefFoundError: '" + ncdf.getMessage() + "' parsing " + currentLine);
				}
			}
		}
	}

	public String convertNativeCodeMethodName(String name)
	{
		name = name.replace("'", "");

		int methodMarkIndex = name.indexOf(JITWatchConstants.NATIVE_CODE_METHOD_MARK);

		if (methodMarkIndex != -1)
		{
			name = name.substring(methodMarkIndex + JITWatchConstants.NATIVE_CODE_METHOD_MARK.length());
			name = name.trim();
		}

		String inToken = " in ";

		int inPos = name.indexOf(inToken);

		if (inPos != -1)
		{
			name = name.substring(inPos + inToken.length()) + " " + name.substring(0, inPos);
		}

		name = name.replaceAll("/", ".");

		return name;
	}
}