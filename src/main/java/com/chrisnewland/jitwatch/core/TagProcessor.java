/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.core;

import com.chrisnewland.jitwatch.model.CompilerName;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.model.Task;
import com.chrisnewland.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class TagProcessor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TagProcessor.class);

	// feed it lines until it completes a tag
	private Tag currentTag;
	private Tag topTag = null;
	private CompilerName currentCompiler;
	
	//TODO write own mini XPath?
	
	public void setCompiler(CompilerName compiler)
	{
		currentCompiler = compiler;
	}
	
	public CompilerName getCompiler()
	{
		return currentCompiler;
	}
	
	public Tag processLine(String line)
	{
		Tag result = null;

		if (line != null)
		{
			if (line.length() > 3 && line.charAt(0) == C_OPEN_ANGLE)
			{
				result = handleTag(line);
			}
			else if (currentTag != null)
			{
				currentTag.addTextContent(line);
			}
			else
			{
                LOGGER.error("Did not handle: {}", line);
			}
		}

		if (result != null)
		{
			currentTag = null;
			topTag = null;
		}

		
		return result;
	}

	private Tag handleTag(String line)
	{
		Tag result = null;

		// closing tag
		if (line.charAt(1) == C_SLASH)
		{
			String closeName = line.substring(2, line.length() - 1);

			if (closeName.equals(currentTag.getName()))
			{
				if (currentTag.getParent() == null)
				{
					result = currentTag;
				}
				else
				{
					currentTag = currentTag.getParent();
				}
			}
		}
		else
		{
			int indexEndName = line.indexOf(C_SPACE);

			if (indexEndName == -1)
			{
				indexEndName = line.indexOf(C_CLOSE_ANGLE);
			}

			if (indexEndName != -1)
			{
				result = processValidLine(line, indexEndName);
			}
		}

		return result;
	}

	private Tag processValidLine(String line, int indexEndName)
	{
		Tag result = null;

		String name = line.substring(1, indexEndName);

		String remainder = line.substring(indexEndName);

		Map<String, String> attrs = StringUtil.getLineAttributes(remainder);

		boolean selfClosing = (line.charAt(line.length() - 2) == C_SLASH);

		Tag t;

		if (JITWatchConstants.TAG_TASK.equals(name))
		{
			t = new Task(name, attrs, selfClosing, currentCompiler);
		}
		else
		{
			t = new Tag(name, attrs, selfClosing);
		}

		if (currentTag == null)
		{
			// new tag at top level
			currentTag = t;
			topTag = t;
		}
		else
		{
			currentTag.addChild(t);
		}

		if (topTag instanceof Task)
		{
			switch (name)
			{
			case JITWatchConstants.TAG_TYPE:
				((Task) topTag).addDictionaryType(attrs.get(JITWatchConstants.ATTR_ID), t);
				break;

			case JITWatchConstants.TAG_METHOD:
				((Task) topTag).addDictionaryMethod(attrs.get(JITWatchConstants.ATTR_ID), t);
				break;

			case JITWatchConstants.TAG_KLASS:
				((Task) topTag).addDictionaryKlass(attrs.get(JITWatchConstants.ATTR_ID), t);
				break;

            default:
                break;
			}
		}

		if (selfClosing &&
		    (name.equals(currentTag.getName())) &&
		    (currentTag.getParent() == null))
        {
                result = currentTag;
        }
		else
		{
			// not closed
			currentTag = t;
		}

		return result;
	}
}