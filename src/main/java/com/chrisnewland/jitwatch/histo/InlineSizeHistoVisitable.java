/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.histo;

import com.chrisnewland.jitwatch.model.*;
import com.chrisnewland.jitwatch.util.JournalUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class InlineSizeHistoVisitable extends AbstractHistoVisitable
{
	private Set<String> inlinedCounted = new HashSet<>();
	private IParseDictionary parseDictionary;

	public InlineSizeHistoVisitable(IReadOnlyJITDataModel model, long resolution)
	{
		super(model, resolution);
	}

	@Override
	public void reset()
	{
		inlinedCounted.clear();
		parseDictionary = null;
	}

	@Override
	public void visit(IMetaMember mm)
	{
		if (mm.isCompiled())
		{
			Journal journal = mm.getJournal();

			Task lastTaskTag = JournalUtil.getLastTask(journal);

			if (lastTaskTag != null)
			{
				parseDictionary = lastTaskTag.getParseDictionary();
			}

			Tag parsePhase = JournalUtil.getParsePhase(journal);

			// TODO fix for JDK8
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
		String currentMethod = null;
		String holder = null;
		String attrInlineBytes = null;

		for (Tag child : parseTag.getChildren())
		{
			String tagName = child.getName();
			Map<String, String> attrs = child.getAttrs();

			switch (tagName)
			{
			case TAG_METHOD:
			{
				currentMethod = attrs.get(ATTR_NAME);
				holder = attrs.get(ATTR_HOLDER);
				attrInlineBytes = attrs.get(ATTR_BYTES);
			}
				break;

			case TAG_INLINE_FAIL:
			{
				// clear method to prevent incorrect pickup by next inline
				// success
				currentMethod = null;
				holder = null;
				attrInlineBytes = null;
			}
				break;

			case TAG_INLINE_SUCCESS:
			{
				if (holder != null && currentMethod != null && attrInlineBytes != null)
				{
					Tag klassTag = parseDictionary.getKlass(holder);

					if (klassTag != null)
					{
						String fqName = klassTag.getAttribute(ATTR_NAME) + C_SLASH + currentMethod;

						if (!inlinedCounted.contains(fqName))
						{
							long inlinedByteCount = Long.parseLong(attrInlineBytes);
							histo.addValue(inlinedByteCount);

							inlinedCounted.add(fqName);
						}
					}
				}
			}
				break;
			case TAG_PARSE:
			{
				processParseTag(child);
			}
				break;

            default:
                break;
			}
		}
	}
}
