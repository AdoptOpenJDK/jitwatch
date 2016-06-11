/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.intrinsic;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.journal.AbstractJournalVisitable;
import org.adoptopenjdk.jitwatch.journal.JournalUtil;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IntrinsicFinder extends AbstractJournalVisitable
{
	private Map<String, String> result;

	private static final Logger logger = LoggerFactory.getLogger(IntrinsicFinder.class);

	public IntrinsicFinder()
	{
		ignoreTags.add(TAG_BC);
		ignoreTags.add(TAG_KLASS);
		ignoreTags.add(TAG_TYPE);
		ignoreTags.add(TAG_UNCOMMON_TRAP);
		ignoreTags.add(TAG_PARSE_DONE);
		ignoreTags.add(TAG_BRANCH);
		ignoreTags.add(TAG_CAST_UP);
		ignoreTags.add(TAG_PARSE);
		ignoreTags.add(TAG_INLINE_SUCCESS);
		ignoreTags.add(TAG_INLINE_FAIL);
		ignoreTags.add(TAG_DIRECT_CALL);
		ignoreTags.add(TAG_PREDICTED_CALL);
		ignoreTags.add(TAG_DEPENDENCY);	
	}

	public Map<String, String> findIntrinsics(IMetaMember member)
	{
		result = new HashMap<>();

		if (member != null)
		{
			try
			{
				JournalUtil.visitParseTagsOfLastTask(member.getJournal(), this);
			}
			catch (LogParseException e)
			{
				logger.error("Error while finding intrinsics for member {}", member, e);
			}
		}

		return result;
	}

	@Override
	public void visitTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
		String currentMethod = null;
		String holder = null;

		List<Tag> allChildren = parseTag.getChildren();

		for (Tag child : allChildren)
		{
			String tagName = child.getName();
			Map<String, String> attrs = child.getAttributes();

			switch (tagName)
			{
			case TAG_METHOD:
			{
				currentMethod = attrs.get(ATTR_NAME);
				holder = attrs.get(ATTR_HOLDER);
				break;
			}

				// changes member context
			case TAG_CALL:
			{
				String methodID = attrs.get(ATTR_METHOD);

				Tag methodTag = parseDictionary.getMethod(methodID);
				currentMethod = methodTag.getAttribute(ATTR_NAME);
				holder = methodTag.getAttribute(ATTR_HOLDER);
				break;
			}

			case TAG_INTRINSIC:
			{
				if (holder != null && currentMethod != null)
				{
					Tag klassTag = parseDictionary.getKlass(holder);

					String intrinsic = child.getAttribute(ATTR_ID);

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

			case TAG_PHASE:
			{
				String phaseName = attrs.get(ATTR_NAME);

				if (S_PARSE_HIR.equals(phaseName))
				{
					visitTag(child, parseDictionary);
				}
				else
				{
					logger.warn("Don't know how to handle phase {}", phaseName);
				}

				break;
			}

			default:
				handleOther(child);
				break;
			}
		}
	}
}