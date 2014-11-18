/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_HOLDER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.journal.ILastTaskParseTagVisitable;
import org.adoptopenjdk.jitwatch.journal.JournalUtil;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IntrinsicFinder implements ILastTaskParseTagVisitable
{
	private Map<String, String> result;

	private static final Logger logger = LoggerFactory.getLogger(IntrinsicFinder.class);

	public IntrinsicFinder()
	{
	}

	public Map<String, String> findIntrinsics(IMetaMember member)
	{
		result = new HashMap<>();

		try
		{
			JournalUtil.visitParseTagsOfLastTask(member, this);
		}
		catch (LogParseException e)
		{
			logger.error("Error while finding intrinsics", e);
		}

		return result;
	}

	@Override
	public void visitParseTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
		String currentMethod = null;
		String holder = null;

		List<Tag> allChildren = parseTag.getChildren();

		for (Tag childTag : allChildren)
		{
			String tagName = childTag.getName();
			Map<String, String> attrs = childTag.getAttrs();

			switch (tagName)
			{
			case TAG_METHOD:
			{
				currentMethod = attrs.get(ATTR_NAME);
				holder = attrs.get(ATTR_HOLDER);
			}
				break;

			// changes member context
			case TAG_CALL:
			{
				String methodID = attrs.get(ATTR_METHOD);

				Tag methodTag = parseDictionary.getMethod(methodID);
				currentMethod = methodTag.getAttribute(ATTR_NAME);
				holder = methodTag.getAttribute(ATTR_HOLDER);
			}
				break;

			case TAG_INTRINSIC:
			{
				if (holder != null && currentMethod != null)
				{
					Tag klassTag = parseDictionary.getKlass(holder);

					String intrinsic = childTag.getAttribute(ATTR_ID);

					if (klassTag != null)
					{
						String fqName = klassTag.getAttribute(ATTR_NAME).replace(C_SLASH, C_DOT) + C_DOT + currentMethod;

						result.put(fqName, intrinsic);
					}
				}

				holder = null;
				currentMethod = null;
				break;
			}

			default:
			{
				break;
			}
			}
		}
	}
}
