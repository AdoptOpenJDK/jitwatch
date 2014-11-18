/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_REASON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.journal.ILastTaskParseTagVisitable;
import org.adoptopenjdk.jitwatch.journal.JournalUtil;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;

public class InliningFailReasonTopListVisitable extends AbstractTopListVisitable implements ILastTaskParseTagVisitable
{
	private final Map<String, Integer> reasonCountMap;

	public InliningFailReasonTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
		reasonCountMap = new HashMap<>();
	}

	@Override
	public void visit(IMetaMember metaMember)
	{
		if (metaMember.isCompiled())
		{
			try
			{
				JournalUtil.visitParseTagsOfLastTask(metaMember, this);
			}
			catch (LogParseException e)
			{
				logger.error("Error building inlining stats", e);
			}
		}
	}

	private void processParseTag(Tag parseTag)
	{
		for (Tag child : parseTag.getChildren())
		{
			String tagName = child.getName();
			Map<String, String> attrs = child.getAttrs();

			switch (tagName)
			{
			case TAG_INLINE_FAIL:
			{
				String reason = attrs.get(ATTR_REASON);

				if (reasonCountMap.containsKey(reason))
				{
					int count = reasonCountMap.get(reason);
					reasonCountMap.put(reason, count + 1);
				}
				else
				{
					reasonCountMap.put(reason, 1);
				}
				break;
			}
			case TAG_PARSE:
			{
				processParseTag(child);
				break;
			}
			default:
				break;
			}
		}
	}

	@Override
	public void postProcess()
	{
		for (Map.Entry<String, Integer> entry : reasonCountMap.entrySet())
		{
			topList.add(new StringTopListScore(entry.getKey(), entry.getValue().longValue()));
		}
	}

	@Override
	public void visitParseTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
		processParseTag(parseTag);
	}
}