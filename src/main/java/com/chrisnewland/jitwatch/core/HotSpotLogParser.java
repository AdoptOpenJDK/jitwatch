/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.core;

import com.chrisnewland.jitwatch.model.*;
import com.chrisnewland.jitwatch.util.ClassUtil;
import com.chrisnewland.jitwatch.util.DisassemblyUtil;
import com.chrisnewland.jitwatch.util.ParseUtil;
import com.chrisnewland.jitwatch.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class HotSpotLogParser
{
	enum ParseState
	{
		READY, IN_TAG, IN_NATIVE
	}

    private static final Logger logger = LoggerFactory.getLogger(HotSpotLogParser.class);

	private JITDataModel model;

	private boolean watching = false;

	private ParseState parseState = ParseState.READY;

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
		
		logger.info("hsdis available: {}", DisassemblyUtil.isDisassemblerAvailable());
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
				catch (Exception ex)
				{
                    logger.error("Exception handling: '{}' {}", currentLine, ex);
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
					logger.error("{}", e);
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
		currentLine = currentLine.replace("&lt;", S_OPEN_ANGLE);
		currentLine = currentLine.replace("&gt;", S_CLOSE_ANGLE);

		if (currentLine.startsWith(S_OPEN_ANGLE))
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

					parseState = ParseState.READY;
				}
				else
				{
					parseState = ParseState.IN_TAG;
				}
			}
		}
		else if (currentLine.startsWith(JITWatchConstants.LOADED))
		{
			if (parseState == ParseState.IN_NATIVE)
			{
				completeNativeCode();
			}
			handleLoaded(currentLine);
		}
		else if (parseState == ParseState.IN_NATIVE)
		{
			appendNativeCode(currentLine);
		}
		else if (parseState == ParseState.IN_TAG)
		{
			tagProcessor.processLine(currentLine);
		}
		else if (currentLine.contains(JITWatchConstants.NATIVE_CODE_METHOD_MARK))
		{
			String sig = ParseUtil.convertNativeCodeMethodName(currentLine);

			currentMember = findMemberWithSignature(sig);

			parseState = ParseState.IN_NATIVE;

			appendNativeCode(currentLine);
		}

		currentLineNumber++;
	}

	private void handleTag(Tag tag)
	{
		if (parseState == ParseState.IN_NATIVE)
		{
			// TODO: chase up bug report for mangled hotspot output
			completeNativeCode();
		}

		String tagName = tag.getName();

		switch (tagName)
		{
		case JITWatchConstants.TAG_VM_VERSION:
			handleVmVersion(tag);
			break;
		
		case JITWatchConstants.TAG_TASK_QUEUED:
			handleTagQueued(tag);
			break;

		case JITWatchConstants.TAG_NMETHOD:
			handleTagNMethod(tag);
			break;

		case JITWatchConstants.TAG_TASK:
			handleTagTask(tag);
			break;

		case JITWatchConstants.TAG_START_COMPILE_THREAD:
			handleStartCompileThread(tag);
			break;

        default:
            break;
		}
	}

	private void handleVmVersion(Tag tag)
	{
		String release = tag.getNamedChildren(TAG_RELEASE).get(0).getTextContent();

		model.setVmVersionRelease(release);
	}

	private void appendNativeCode(String line)
	{
		nativeCodeBuilder.append(line).append("\n");
	}

	private void completeNativeCode()
	{
		parseState = ParseState.READY;

		if (currentMember != null)
		{
			currentMember.setAssembly(nativeCodeBuilder.toString());
		}

		nativeCodeBuilder.delete(0, nativeCodeBuilder.length());
	}

	private void handleStartCompileThread(Tag tag)
	{
		model.getJITStats().incCompilerThreads();
		String threadName = tag.getAttribute(ATTR_NAME);

        if (theThreadIsNotFound(threadName)) {
            logger.error("Thread name not found (attribute '{}' missing in tag).\n", ATTR_NAME);
            return;
        }

		if (threadName.startsWith(C1))
		{
			tagProcessor.setCompiler(CompilerName.C1);
		}
		else if (threadName.startsWith(C2))
		{
			tagProcessor.setCompiler(CompilerName.C2);
		}
		else
		{
            logger.error("Unexpected compiler name: {}", threadName);
		}
	}

    private boolean theThreadIsNotFound(String threadName) {
        return threadName == null;
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

	private void handleTagQueued(Tag tag)
	{
		handleMethodLine(tag, EventType.QUEUE);
	}

	private void handleTagNMethod(Tag tag)
	{
		String attrCompiler = tag.getAttribute(ATTR_COMPILER);

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
			String attrCompileKind = tag.getAttribute(ATTR_COMPILE_KIND);
			
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

	private void handleTagTask(Tag tag)
	{
		handleMethodLine(tag, EventType.TASK);

		Tag tagCodeCache = tag.getFirstNamedChild(JITWatchConstants.TAG_CODE_CACHE);

		if (tagCodeCache != null)
		{
			// copy timestamp from parent <task> tag used for graphing code
			// cache
			String stamp = tag.getAttribute(JITWatchConstants.ATTR_STAMP);
			tagCodeCache.getAttrs().put(JITWatchConstants.ATTR_STAMP, stamp);

			model.addCodeCacheTag(tagCodeCache);
		}

		Tag tagTaskDone = tag.getFirstNamedChild(JITWatchConstants.TAG_TASK_DONE);

		if (tagTaskDone != null)
		{
			handleTaskDone(tagTaskDone);
		}
	}

	private void handleMethodLine(Tag tag, EventType eventType)
	{
		Map<String, String> attrs = tag.getAttrs();

		String attrMethod = attrs.get(ATTR_METHOD);

		if (attrMethod != null)
		{
			attrMethod = attrMethod.replace(S_SLASH, S_DOT);

			IMetaMember member = handleMember(attrMethod, attrs, eventType);

			if (member != null)
			{
				member.addJournalEntry(tag);
			}
		}
	}

	private IMetaMember handleMember(String signature, Map<String, String> attrs, EventType type)
	{
		IMetaMember metaMember = findMemberWithSignature(signature);

		String stampAttr = attrs.get(ATTR_STAMP);
		long stampTime = ParseUtil.parseStamp(stampAttr);

		if (metaMember != null)
		{
			switch (type)
			{
			case QUEUE:
			{
				metaMember.setQueuedAttributes(attrs);
				JITEvent queuedEvent = new JITEvent(stampTime, type, metaMember.toString());
				model.addEvent(queuedEvent);
				logEvent(queuedEvent);
			}
				break;
			case NMETHOD_C1:
			case NMETHOD_C2:
			case NMETHOD_C2N:
			{
				metaMember.setCompiledAttributes(attrs);
				metaMember.getMetaClass().incCompiledMethodCount();
				model.updateStats(metaMember);

				JITEvent compiledEvent = new JITEvent(stampTime, type, metaMember.toString());
				model.addEvent(compiledEvent);
				logEvent(compiledEvent);
			}
				break;
			case TASK:
			{
				metaMember.addCompiledAttributes(attrs);
				currentMember = metaMember;
			}
				break;
			}
		}

		return metaMember;
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
		String fqClassName = StringUtil.getSubstringBetween(currentLine, LOADED, S_SPACE);

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
				packageName = S_EMPTY;
				className = fqClassName;
			}

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