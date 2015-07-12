/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.journal;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BUILDIR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_HOLDER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C2N;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CLOSE_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CONSTRUCTOR_INIT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ENTITY_GT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ENTITY_LT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPTIMIZER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ELIMINATE_ALLOCATION;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE;

import java.util.List;

import org.adoptopenjdk.jitwatch.model.CompilerName;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JournalUtil
{
	private static final Logger logger = LoggerFactory.getLogger(JournalUtil.class);

	private JournalUtil()
	{
	}

	public static void visitParseTagsOfLastTask(IMetaMember member, IJournalVisitable visitable) throws LogParseException
	{
		if (member == null)
		{
			throw new LogParseException("Cannot get Journal for null IMetaMember");
		}

		Journal journal = member.getJournal();

		Task lastTask = getLastTask(journal);

		if (lastTask == null)
		{
			if (!isJournalForCompile2NativeMember(journal))
			{
				logger.warn("No Task found in Journal for member {}", member);

				if (journal != null && journal.getEntryList().size() > 0)
				{
					logger.warn(journal.toString());
				}
			}
		}
		else
		{
			IParseDictionary parseDictionary = lastTask.getParseDictionary();

			Tag parsePhase = getParsePhase(lastTask);

			if (parsePhase != null)
			{
				List<Tag> parseTags = parsePhase.getNamedChildren(TAG_PARSE);

				if (DEBUG_LOGGING)
				{
					logger.debug("About to visit {} parse tags in last <task> of Journal", parseTags.size());
				}

				for (Tag parseTag : parseTags)
				{
					visitable.visitTag(parseTag, parseDictionary);
				}
			}
		}
	}

	public static void visitOptimizerTagsOfLastTask(IMetaMember member, IJournalVisitable visitable) throws LogParseException
	{
		if (member == null)
		{
			throw new LogParseException("Cannot get Journal for null IMetaMember");
		}

		Journal journal = member.getJournal();

		Task lastTask = getLastTask(journal);

		if (lastTask == null)
		{
			if (!isJournalForCompile2NativeMember(journal))
			{
				logger.warn("No Task found in Journal for member {}", member);

				if (journal != null && journal.getEntryList().size() > 0)
				{
					logger.warn(journal.toString());
				}
			}
		}
		else
		{
			IParseDictionary parseDictionary = lastTask.getParseDictionary();

			Tag optimizerPhase = getOptimizerPhase(lastTask);
			
			if (optimizerPhase != null)
			{
				List<Tag> eliminateAllocationTags = optimizerPhase.getNamedChildren(TAG_ELIMINATE_ALLOCATION);

				for (Tag eliminationTag : eliminateAllocationTags)
				{
					visitable.visitTag(eliminationTag, parseDictionary);
				}
			}
		}
	}

	
	public static boolean isJournalForCompile2NativeMember(Journal journal)
	{
		boolean result = false;

		if (journal != null)
		{
			List<Tag> entryList = journal.getEntryList();

			if (entryList.size() >= 1)
			{
				Tag tag = entryList.get(0);

				String tagName = tag.getName();

				if (TAG_NMETHOD.equals(tagName))
				{
					if (C2N.equals(tag.getAttribute(ATTR_COMPILE_KIND)))
					{
						result = true;
					}
				}
			}
		}

		return result;
	}

	public static boolean memberMatchesParseTag(IMetaMember member, Tag parseTag, IParseDictionary parseDictionary)
	{
		boolean result = false;

		if (DEBUG_LOGGING)
		{
			logger.debug("memberMatchesParseTag: {}", parseTag.toString(false));
		}

		String methodID = parseTag.getAttribute(ATTR_METHOD);

		Tag methodTag = parseDictionary.getMethod(methodID);

		if (DEBUG_LOGGING)
		{
			logger.debug("methodTag: {}", methodTag.toString(true));
		}

		if (methodTag != null)
		{
			String klassID = methodTag.getAttribute(ATTR_HOLDER);

			Tag klassTag = parseDictionary.getKlass(klassID);

			if (klassTag != null)
			{
				if (DEBUG_LOGGING)
				{
					logger.debug("klass tag: {}", klassTag.toString(false));
				}

				String klassAttrName = klassTag.getAttribute(ATTR_NAME);
				String methodAttrName = methodTag.getAttribute(ATTR_NAME).replace(S_ENTITY_LT, S_OPEN_ANGLE).replace(S_ENTITY_GT, S_CLOSE_ANGLE);		
				
				if (klassAttrName != null)
				{
					klassAttrName = klassAttrName.replace(C_SLASH, C_DOT);
				}

				String returnType = ParseUtil.getMethodTagReturn(methodTag, parseDictionary);
				List<String> paramTypes = ParseUtil.getMethodTagArguments(methodTag, parseDictionary);

				if (DEBUG_LOGGING)
				{
					logger.debug("memberName: {}/{}", member.getMemberName(), methodAttrName);
					logger.debug("metaClass : {}/{}", member.getMetaClass().getFullyQualifiedName(), klassAttrName);
					logger.debug("return    : {}/{}", member.getReturnTypeName(), returnType);
					logger.debug("params    : {}/{}", StringUtil.arrayToString(member.getParamTypeNames()),
							StringUtil.listToString(paramTypes));
				}

				boolean nameMatches;
							
				if (S_CONSTRUCTOR_INIT.equals(methodAttrName))
				{
					nameMatches = member.getMemberName().equals(klassAttrName);
				}
				else
				{
					nameMatches = member.getMemberName().equals(methodAttrName);
				}
				
				boolean klassMatches = member.getMetaClass().getFullyQualifiedName().equals(klassAttrName);
				boolean returnMatches = member.getReturnTypeName().equals(returnType);

				boolean paramsMatch = true;

				if (member.getParamTypeNames().length == paramTypes.size())
				{
					for (int pos = 0; pos < member.getParamTypeNames().length; pos++)
					{
						String memberParamType = member.getParamTypeNames()[pos];
						String tagParamType = paramTypes.get(pos);

						// logger.debug("checking: {}/{}", memberParamType,
						// tagParamType);

						if (!memberParamType.equals(tagParamType))
						{
							paramsMatch = false;
							break;
						}
					}
				}
				else
				{
					paramsMatch = false;
				}

				result = nameMatches && klassMatches && returnMatches && paramsMatch;

				if (DEBUG_LOGGING)
				{
					logger.debug("Matched name: {} klass: {} return: {} params: {}", nameMatches, klassMatches, returnMatches,
							paramsMatch);

					logger.debug("Matches member:{} = {}", member, result);
				}
			}
		}

		return result;
	}

	public static Task getLastTask(Journal journal)
	{
		Task lastTask = null;

		if (journal != null)
		{
			for (Tag tag : journal.getEntryList())
			{
				if (tag instanceof Task)
				{
					lastTask = (Task) tag;
				}
			}
		}

		return lastTask;
	}

	public static CompilerName getCompilerNameForLastTask(Journal journal)
	{
		Task lastTask = getLastTask(journal);

		CompilerName compilerName = null;

		if (lastTask != null)
		{
			compilerName = lastTask.getCompiler();
		}

		return compilerName;
	}

	private static Tag getParsePhase(Task lastTask)
	{
		Tag parsePhase = null;

		if (lastTask != null)
		{
			CompilerName compilerName = lastTask.getCompiler();

			String parseAttributeName = ATTR_PARSE;

			if (compilerName == CompilerName.C1)
			{
				parseAttributeName = ATTR_BUILDIR;
			}

			List<Tag> parsePhases = lastTask.getNamedChildrenWithAttribute(TAG_PHASE, ATTR_NAME, parseAttributeName);

			int count = parsePhases.size();

			if (count != 1)
			{
				logger.warn("Unexpected parse phase count: {}", count);
			}
			else
			{
				parsePhase = parsePhases.get(0);
			}
		}

		return parsePhase;
	}
	
	private static Tag getOptimizerPhase(Task lastTask)
	{
		Tag optimizerPhase = null;

		if (lastTask != null)
		{
			CompilerName compilerName = lastTask.getCompiler();

			List<Tag> parsePhases = lastTask.getNamedChildrenWithAttribute(TAG_PHASE, ATTR_NAME, S_OPTIMIZER);

			int count = parsePhases.size();

			if (count != 1)
			{
				logger.warn("Unexpected optimizer phase count: {}", count);
			}
			else
			{
				optimizerPhase = parsePhases.get(0);
			}
		}

		return optimizerPhase;
	}
}