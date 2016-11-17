/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.compilation;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BUILDIR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_HOLDER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C2N;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CONSTRUCTOR_INIT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPTIMIZER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ELIMINATE_ALLOCATION;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_FAILURE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_REASON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_REASON_STALE_TASK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_METHOD_ID_MATCH;

import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CompilationUtil
{
	private static final Logger logger = LoggerFactory.getLogger(CompilationUtil.class);

	private static int unhandledTagCount = 0;

	private CompilationUtil()
	{
	}

	public static void visitParseTagsOfCompilation(Compilation compilation, ICompilationVisitable visitable)
			throws LogParseException
	{
		if (compilation != null)
		{
			Task tagTask = compilation.getTagTask();

			if (tagTask == null)
			{
				if (!compilation.isC2N())
				{
					logger.warn("No Task found in Compilation {}", compilation.getCompileID());
				}
			}
			else
			{
				IParseDictionary parseDictionary = tagTask.getParseDictionary();

				Tag parsePhase = getParsePhase(tagTask);

				if (parsePhase != null)
				{
					List<Tag> parseTags = parsePhase.getNamedChildren(TAG_PARSE);

					if (DEBUG_LOGGING)
					{
						logger.debug("About to visit {} parse tags of <task>", parseTags.size());
					}

					for (Tag parseTag : parseTags)
					{
						visitable.visitTag(parseTag, parseDictionary);
					}
				}
			}
		}
		else
		{
			logger.warn("Compilation is null");
		}
	}

	public static void visitOptimizerTagsOfCompilation(Compilation compilation, ICompilationVisitable visitable)
			throws LogParseException
	{
		if (compilation != null)
		{
			Task tagTask = compilation.getTagTask();

			if (tagTask == null)
			{
				if (!compilation.isC2N())
				{
					logger.warn("No Task found in Compilation {}", compilation.getCompileID());
				}
			}
			else
			{
				IParseDictionary parseDictionary = tagTask.getParseDictionary();

				Tag optimizerPhase = getOptimizerPhase(tagTask);

				if (optimizerPhase != null)
				{
					for (Tag child : optimizerPhase.getChildren())
					{
						visitable.visitTag(child, parseDictionary);
					}
				}
			}
		}
		else
		{
			logger.warn("Compilation is null");
		}
	}

	public static void visitEliminationTagsOfCompilation(Compilation compilation, ICompilationVisitable visitable)
			throws LogParseException
	{
		if (compilation != null)
		{
			Task tagTask = compilation.getTagTask();

			if (tagTask == null)
			{
				if (!compilation.isC2N())
				{
					logger.warn("No Task found in Compilation {}", compilation.getCompileID());
				}
			}
			else
			{
				IParseDictionary parseDictionary = tagTask.getParseDictionary();

				for (Tag child : tagTask.getNamedChildren(TAG_ELIMINATE_ALLOCATION))
				{
					visitable.visitTag(child, parseDictionary);
				}
			}
		}
		else
		{
			logger.warn("Compilation is null");
		}
	}

	// private Map<Integer, Integer> getBytecodeMap

	public static boolean isJournalForCompile2NativeMember(Tag tag)
	{
		boolean result = false;

		if (tag != null)
		{
			String tagName = tag.getName();

			if (TAG_NMETHOD.equals(tagName))
			{
				if (C2N.equals(tag.getAttributes().get(ATTR_COMPILE_KIND)))
				{
					result = true;
				}
			}
		}

		return result;
	}

	public static boolean memberMatchesKlassID(IMetaMember member, String klassID, IParseDictionary parseDictionary)
	{
		boolean result = false;

		String klassName = ParseUtil.lookupType(klassID, parseDictionary);

		String memberClassName = member.getMetaClass().getFullyQualifiedName();

		result = memberClassName.equals(klassName);

		return result;
	}

	public static boolean memberMatchesMethodID(IMetaMember member, String methodID, IParseDictionary parseDictionary)
	{
		boolean result = false;

		Tag methodTag = parseDictionary.getMethod(methodID);

		StringBuilder builder = null;

		if (DEBUG_LOGGING_METHOD_ID_MATCH)
		{
			builder = new StringBuilder();
			builder.append(
					String.format("methodID: %s methodTag: %s", methodID, methodTag != null ? methodTag.toString(true) : "null"))
					.append(S_NEWLINE);
			builder.append(String.format("member: %s ", member.toString())).append(S_NEWLINE);
		}

		if (methodTag != null)
		{
			Map<String, String> methodTagAttributes = methodTag.getAttributes();

			String klassID = methodTagAttributes.get(ATTR_HOLDER);

			Tag klassTag = parseDictionary.getKlass(klassID);

			if (klassTag != null)
			{
				if (DEBUG_LOGGING_METHOD_ID_MATCH)
				{
					builder.append(String.format("klass tag: %s", klassTag.toString(false))).append(S_NEWLINE);
				}

				String klassAttrName = klassTag.getAttributes().get(ATTR_NAME);
				String methodAttrName = StringUtil.replaceXMLEntities(methodTagAttributes.get(ATTR_NAME));

				if (klassAttrName != null)
				{
					klassAttrName = klassAttrName.replace(C_SLASH, C_DOT);
				}

				String returnType = ParseUtil.getMethodTagReturn(methodTag, parseDictionary);
				List<String> paramTypes = ParseUtil.getMethodTagArguments(methodTag, parseDictionary);

				if (DEBUG_LOGGING_METHOD_ID_MATCH)
				{
					builder.append(String.format("memberName: %s/%s", member.getMemberName(), methodAttrName)).append(S_NEWLINE);
					builder.append(String.format("metaClass : %s/%s", member.getMetaClass().getFullyQualifiedName(), klassAttrName))
							.append(S_NEWLINE);
					builder.append(String.format("return    : %s/%s", member.getReturnTypeName(), returnType)).append(S_NEWLINE);
					builder.append(String.format("params    : %s/%s", StringUtil.arrayToString(member.getParamTypeNames()),
							StringUtil.listToString(paramTypes))).append(S_NEWLINE);
				}

				boolean nameMatches;

				if (S_CONSTRUCTOR_INIT.equals(methodAttrName))
				{
					if (DEBUG_LOGGING_METHOD_ID_MATCH)
					{
						builder.append(
								String.format("Looks like a constructor. Checking %s vs %s", member.getMemberName(), klassAttrName))
								.append(S_NEWLINE);
					}

					String unqualifiedClassName = StringUtil.getUnqualifiedClassName(klassAttrName);

					nameMatches = member.getMemberName().equals(unqualifiedClassName);
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

						if (DEBUG_LOGGING_METHOD_ID_MATCH)
						{
							builder.append(String.format("checking: %s/%s", memberParamType, tagParamType)).append(S_NEWLINE);
						}

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

				if (DEBUG_LOGGING_METHOD_ID_MATCH)
				{
					builder.append(String.format("Matched name: %s klass: %s return: %s params: %s", nameMatches, klassMatches,
							returnMatches, paramsMatch)).append(S_NEWLINE);

					builder.append(String.format("Matches member:%s = %s", member, result)).append(S_NEWLINE);

					if (!result)
					{
						logger.error(builder.toString());
					}
				}
			}
		}

		return result;
	}

	public static Tag getParsePhase(Task task)
	{
		Tag parsePhase = null;

		if (task != null)
		{
			// for C1 look for <phase name='buildIR' stamp='18.121'>
			// for C2 look for <phase name='parse' nodes='3' live='3'
			// stamp='11.237'>

			List<Tag> phasesBuildIR = task.getNamedChildrenWithAttribute(TAG_PHASE, ATTR_NAME, ATTR_BUILDIR);

			if (phasesBuildIR.size() == 1)
			{
				parsePhase = phasesBuildIR.get(0);
			}
			else
			{
				List<Tag> phasesParse = task.getNamedChildrenWithAttribute(TAG_PHASE, ATTR_NAME, ATTR_PARSE);

				if (phasesParse.size() == 1)
				{
					parsePhase = phasesParse.get(0);
				}
				else
				{
					if (!isStaleTask(task))
					{
						logger.warn("Unexpected parse phase count: buildIR({}), parse({})", phasesBuildIR.size(),
								phasesParse.size());

						if (DEBUG_LOGGING)
						{
							logger.debug("Task {}", task);
						}

						// possible JDK9 new format with no wrapping tag so
						// return
						// the whole task tag
						parsePhase = task;
					}
				}
			}
		}

		return parsePhase;
	}

	public static boolean isStaleTask(Task task)
	{
		List<Tag> failureChildren = task.getNamedChildren(TAG_FAILURE);

		boolean stale = false;

		for (Tag failure : failureChildren)
		{
			String reason = failure.getAttributes().get(ATTR_REASON);

			if (S_REASON_STALE_TASK.equals(reason))
			{
				stale = true;
				break;
			}
		}

		return stale;
	}

	private static Tag getOptimizerPhase(Task lastTask)
	{
		Tag optimizerPhase = null;

		if (lastTask != null)
		{
			List<Tag> parsePhases = lastTask.getNamedChildrenWithAttribute(TAG_PHASE, ATTR_NAME, S_OPTIMIZER);

			int count = parsePhases.size();

			if (count > 1)
			{
				logger.warn("Unexpected optimizer phase count: {}", count);
			}
			else if (count == 1)
			{
				optimizerPhase = parsePhases.get(0);
			}
		}

		return optimizerPhase;
	}

	public static void unhandledTag(ICompilationVisitable visitable, Tag child)
	{
		unhandledTagCount++;
		logger.warn("{} did not handle {}", visitable.getClass().getName(), child.toString(false));
	}

	public static int getUnhandledTagCount()
	{
		return unhandledTagCount;
	}
}
