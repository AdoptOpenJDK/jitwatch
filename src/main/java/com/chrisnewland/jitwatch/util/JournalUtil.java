/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.paint.Color;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.LineAnnotation;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.model.Task;

public class JournalUtil
{
	public static Map<Integer, LineAnnotation> buildBytecodeAnnotations(Journal journal)
	{
		Map<Integer, LineAnnotation> result = new HashMap<>();

		if (journal != null)
		{
			Tag parsePhase = getParsePhase(journal);

			//TODO fix for JDK8
			if (parsePhase != null)
			{
				List<Tag> parseTags = parsePhase.getNamedChildren(TAG_PARSE);

				for (Tag parseTag : parseTags)
				{
					buildParseTagAnnotations(parseTag, result);
				}
			}
		}

		return result;
	}

	private static void buildParseTagAnnotations(Tag parseTag, Map<Integer, LineAnnotation> result)
	{
		List<Tag> children = parseTag.getChildren();

		int currentBytecode = -1;

		Map<String, String> methodAttrs = new HashMap<>();
		Map<String, String> callAttrs = new HashMap<>();

		for (Tag child : children)
		{
			String name = child.getName();
			Map<String, String> tagAttrs = child.getAttrs();

			switch (name)
			{
			case TAG_BC:
			{
				String bci = tagAttrs.get(ATTR_BCI);
				currentBytecode = Integer.parseInt(bci);
				callAttrs.clear();
			}
				break;
			case TAG_CALL:
			{
				callAttrs.clear();
				callAttrs.putAll(tagAttrs);
			}
				break;
			case TAG_METHOD:
			{
				methodAttrs.clear();
				methodAttrs.putAll(tagAttrs);
			}
				break;
			case TAG_INLINE_SUCCESS:
			{
				String reason = tagAttrs.get(ATTR_REASON);
				String annotationText = InlineUtil.buildInlineAnnotationText(true, reason, callAttrs, methodAttrs);
				result.put(currentBytecode, new LineAnnotation(annotationText, Color.GREEN));
			}
				break;
			case TAG_INLINE_FAIL:
			{
				String reason = tagAttrs.get(ATTR_REASON);
				String annotationText = InlineUtil.buildInlineAnnotationText(false, reason, callAttrs, methodAttrs);
				result.put(currentBytecode, new LineAnnotation(annotationText, Color.RED));
			}
				break;
			case TAG_BRANCH:
			{
				String count = tagAttrs.get(ATTR_BRANCH_COUNT);
				String taken = tagAttrs.get(ATTR_BRANCH_TAKEN);
				String notTaken = tagAttrs.get(ATTR_BRANCH_NOT_TAKEN);
				String prob = tagAttrs.get(ATTR_BRANCH_PROB);

				StringBuilder reason = new StringBuilder();

				if (count != null)
				{
					reason.append("Count: ").append(count).append("\n");
				}

				reason.append("Branch taken: ").append(taken).append("\nBranch not taken: ").append(notTaken);

				if (prob != null)
				{
					reason.append("\nProbability: ").append(prob);
				}

				if (!result.containsKey(currentBytecode))
				{
					result.put(currentBytecode, new LineAnnotation(reason.toString(), Color.BLUE));
				}
			}
				break;
			case TAG_INTRINSIC:
			{
				StringBuilder reason = new StringBuilder();
				reason.append("Intrinsic: ").append(tagAttrs.get(ATTR_ID));

				result.put(currentBytecode, new LineAnnotation(reason.toString(), Color.GREEN));

			}
				break;
			}
		}
	}

	public static Task getLastTask(Journal journal)
	{
		// find the latest task tag
		// this is the most recent compile task for the member
		Task lastTask = null;

		for (Tag tag : journal.getEntryList())
		{
			if (tag instanceof Task)
			{
				lastTask = (Task) tag;
			}
		}

		return lastTask;
	}

	public static Tag getParsePhase(Journal journal)
	{
		Tag parsePhase = null;

		Task lastTask = getLastTask(journal);

		if (lastTask != null)
		{
			//TODO fix for JDK8 structure
			List<Tag> parsePhases = lastTask.getNamedChildrenWithAttribute(TAG_PHASE, ATTR_NAME, ATTR_PARSE);

			int count = parsePhases.size();

			if (count != 1)
			{
				System.out.format("Unexpected parse phase count: %d", count);
			}
			else
			{
				parsePhase = parsePhases.get(0);
			}
		}

		return parsePhase;
	}

	public static Journal getJournal(IReadOnlyJITDataModel model, IMetaMember member)
	{
		Journal journal = null;

		String journalID = member.getJournalID();

		if (journalID != null)
		{
			journal = model.getJournal(journalID);

			if (journal == null)
			{
				// try appending compile_kind as OSR does not generate a
				// unique compile_id
				journal = model.getJournal(journalID + OSR);
			}
		}

		return journal;
	}
}
