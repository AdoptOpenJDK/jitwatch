/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_CLOSE_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_TAGPROCESSOR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_FRAGMENT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OPEN_FRAGMENT;

import java.util.Map;
import java.util.Stack;

import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagProcessor
{
	private static final Logger logger = LoggerFactory.getLogger(TagProcessor.class);

	private Stack<String> methodIDStack = new Stack<>();
	
	private Tag currentTag;
	private Tag topTag = null;
	private boolean fragmentSeen;

	public String getTopTagName()
	{
		String result = null;

		if (topTag != null)
		{
			result = topTag.getName();
		}

		return result;
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
				String closingTag = currentTag.getClosingTag();

				if (line.endsWith(closingTag))
				{
					line = line.substring(0, line.length() - closingTag.length());
					currentTag.addTextContent(line);
					processLine(closingTag);
				}
				else
				{
					currentTag.addTextContent(line);
				}
			}
			else
			{
				if (DEBUG_LOGGING)
				{
					logger.debug("Did not handle: {}", line);
				}
			}
		}

		if (DEBUG_LOGGING_TAGPROCESSOR)
		{
			logger.debug("returning tag: {}", result);
		}

		if (result != null)
		{
			resetState();
		}

		return result;
	}

	public boolean wasFragmentSeen()
	{
		return fragmentSeen;
	}

	private void resetState()
	{
		currentTag = null;
		topTag = null;
	}

	private Tag handleTag(String line)
	{
		Tag result = null;

		if (DEBUG_LOGGING_TAGPROCESSOR)
		{
			logger.debug("Handling line: {}", line);
		}

		// closing tag
		if (line.charAt(1) == C_SLASH)
		{
			String closeName = line.substring(2, line.length() - 1);

			if (DEBUG_LOGGING_TAGPROCESSOR)
			{
				logger.debug("closeName:{}, currentTag:{}, topTag:{}", closeName,
						currentTag == null ? "null" : currentTag.getName(), topTag == null ? "null" : topTag.getName());
			}

			if (currentTag != null && closeName.equals(currentTag.getName()))
			{
				if (currentTag.getParent() == null)
				{
					result = currentTag;
				}
				else
				{
					currentTag = currentTag.getParent();
				}
				
				if (JITWatchConstants.TAG_PARSE.equals(currentTag.getName()))
				{
					methodIDStack.pop();					
				}				
			}
			else if (S_FRAGMENT.equals(closeName))
			{
				result = topTag;
			}
		}
		else
		{
			boolean selfClosing = (line.charAt(line.length() - 2) == C_SLASH);

			int indexEndName = line.indexOf(C_SPACE);

			if (indexEndName == -1)
			{
				indexEndName = line.indexOf(C_CLOSE_ANGLE);

				if (indexEndName > 0)
				{
					if (selfClosing)
					{
						indexEndName = line.length() - 2;
					}
				}
			}

			if (indexEndName != -1)
			{
				result = processValidLine(line, indexEndName, selfClosing);
			}
		}

		return result;
	}

	private Tag processValidLine(String line, int indexEndName, boolean selfClosing)
	{
		if (DEBUG_LOGGING_TAGPROCESSOR)
		{
			logger.debug("processValidLine(line:{}, indexEndName:{}, selfClosing:{})", line, indexEndName, selfClosing);
		}

		Tag result = null;

		String name = line.substring(1, indexEndName);

		String attributeString = line.substring(indexEndName);

		Map<String, String> attrs = StringUtil.attributeStringToMap(attributeString);

		Tag nextTag;

		if (JITWatchConstants.TAG_TASK.equals(name))
		{
			nextTag = new Task(attributeString, selfClosing);
		}
		else
		{
			nextTag = new Tag(name, attributeString, selfClosing);
		}

		if (DEBUG_LOGGING_TAGPROCESSOR)
		{
			logger.debug("top: {}", topTag);
		}
		if (DEBUG_LOGGING_TAGPROCESSOR)
		{
			logger.debug("currentTag: {}", currentTag);
		}
		if (DEBUG_LOGGING_TAGPROCESSOR)
		{
			logger.debug("t: {}", nextTag);
		}

		if (currentTag == null)
		{
			if (name.equals(S_FRAGMENT))
			{
				logger.warn(
						"Found a {} in the HotSpot log. The VM exited before the hotspot log was fully written. JIT information may have been lost.",
						TAG_OPEN_FRAGMENT);

				fragmentSeen = true;

				return null;
			}
			else
			{
				// new tag at top level
				currentTag = nextTag;
				topTag = nextTag;
			}
		}
		else
		{
			currentTag.addChild(nextTag);
		}

		if (topTag instanceof Task)
		{
			switch (name)
			{
			case JITWatchConstants.TAG_TYPE:
				((Task) topTag).addDictionaryType(attrs.get(JITWatchConstants.ATTR_ID), nextTag);
				break;

			case JITWatchConstants.TAG_METHOD:
				((Task) topTag).addDictionaryMethod(attrs.get(JITWatchConstants.ATTR_ID), nextTag);
				break;

			case JITWatchConstants.TAG_KLASS:
				((Task) topTag).addDictionaryKlass(attrs.get(JITWatchConstants.ATTR_ID), nextTag);
				break;

			case JITWatchConstants.TAG_PARSE:
				String currentMethodID = attrs.get(JITWatchConstants.ATTR_METHOD);
				methodIDStack.push(currentMethodID);
				break;

			case JITWatchConstants.TAG_BC:
				String bci = attrs.get(JITWatchConstants.ATTR_BCI);
				String code = attrs.get(JITWatchConstants.ATTR_CODE);

				try
				{
					int bciValue = Integer.parseInt(bci);
					int codeValue = Integer.parseInt(code);
					Opcode opcode = Opcode.getByCode(codeValue);

					((Task) topTag).addBCIOpcodeMapping(methodIDStack.peek(), bciValue, opcode);
					
					//logger.info("{} got bc tag {}", methodIDStack.peek(), nextTag.toString(false));
				}
				catch (NumberFormatException nfe)
				{
					logger.error("Couldn't parse bc tag {}", nextTag);
				}

				break;

			default:
				break;
			}
		}

		if (selfClosing)
		{
			if (name.equals(currentTag.getName()))
			{
				if (currentTag.getParent() == null)
				{
					result = currentTag;
				}
			}
		}
		else
		{
			// not closed
			currentTag = nextTag;
		}

		return result;
	}
}
