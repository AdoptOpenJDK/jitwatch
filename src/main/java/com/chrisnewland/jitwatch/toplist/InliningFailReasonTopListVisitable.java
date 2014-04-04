/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.toplist;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.util.JournalUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class InliningFailReasonTopListVisitable extends AbstractTopListVisitable
{
	private final Map<String, Integer> reasonCountMap;

	public InliningFailReasonTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
		reasonCountMap = new HashMap<>();
	}

	@Override
	public void visit(IMetaMember mm)
	{
		if (mm.isCompiled())
		{
			Journal journal = mm.getJournal();

			Tag parsePhase = JournalUtil.getParsePhase(journal);

			if (parsePhase != null)
			{
				List<Tag> parseTags = parsePhase.getNamedChildren(TAG_PARSE);

				for (Tag parseTag : parseTags)
				{
					processParseTag(parseTag);
				}
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
}
