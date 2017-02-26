/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BYTES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COUNT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_HOLDER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_IICOUNT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_PROF_FACTOR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.Map;

import org.adoptopenjdk.jitwatch.model.IParseDictionary;

public final class TooltipUtil
{
	private TooltipUtil()
	{
	}

	public static String buildInlineAnnotationText(boolean inlined, String reason, Map<String, String> callAttrs,
			Map<String, String> methodAttrs, IParseDictionary parseDictionary)
	{
		StringBuilder builder = new StringBuilder();

		String holder = methodAttrs.get(ATTR_HOLDER);
		String methodName = methodAttrs.get(ATTR_NAME);

		builder.append("Class: ").append(ParseUtil.lookupType(holder, parseDictionary)).append(S_NEWLINE);
		builder.append("Method: ").append(StringUtil.replaceXMLEntities(methodName)).append(S_NEWLINE);

		builder.append("Inlined: ");

		if (inlined)
		{
			builder.append("Yes, ");
		}
		else
		{
			builder.append("No, ");
		}

		builder.append(reason);

		if (callAttrs.containsKey(ATTR_COUNT))
		{
			builder.append("\nCount: ").append(callAttrs.get(ATTR_COUNT));
		}
		if (methodAttrs.containsKey(ATTR_IICOUNT))
		{
			builder.append("\niicount: ").append(methodAttrs.get(ATTR_IICOUNT));
		}
		if (methodAttrs.containsKey(ATTR_BYTES))
		{
			builder.append("\nBytes: ").append(methodAttrs.get(ATTR_BYTES));
		}
		if (callAttrs.containsKey(ATTR_PROF_FACTOR))
		{
			builder.append("\nProf factor: ").append(callAttrs.get(ATTR_PROF_FACTOR));
		}

		return builder.toString();
	}
}