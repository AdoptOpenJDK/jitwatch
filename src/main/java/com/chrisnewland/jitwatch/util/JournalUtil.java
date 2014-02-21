/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.paint.Color;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.LineAnnotation;
import com.chrisnewland.jitwatch.model.LineAnnotation.AnnotationType;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.model.Task;

public class JournalUtil
{
	public static Map<Integer, LineAnnotation> buildBytecodeAnnotations(Journal journal)
	{
		Map<Integer, LineAnnotation> result = new HashMap<>();

		if (journal != null)
		{
			List<Tag> parseTags = getParseTags(journal);

			for (Tag parseTag : parseTags)
			{
				List<Tag> children = parseTag.getChildren();

				int currentBytecode = -1;

				Map<String, String> callAttrs = new HashMap<>();
				
				for (Tag child : children)
				{
					String name = child.getName();
					Map<String, String> attrs = child.getAttrs();					

					switch (name)
					{
					case TAG_BC:
					{
						String bci = attrs.get(ATTR_BCI);
						currentBytecode = Integer.parseInt(bci);
						callAttrs.clear();
					}
						break;
					case TAG_CALL:
					{
						callAttrs.putAll(attrs);
					}
						break;
					case TAG_INLINE_SUCCESS:
					{
						StringBuilder reason = new StringBuilder();
						reason.append("Inlined: ").append(attrs.get(ATTR_REASON));
						
						if (callAttrs.containsKey(ATTR_COUNT))
						{
							reason.append("\nCount: ").append(callAttrs.get(ATTR_COUNT));
						}
						if (callAttrs.containsKey(ATTR_PROF_FACTOR))
						{
							reason.append("\nProf factor: ").append(callAttrs.get(ATTR_PROF_FACTOR));
						}
						
						result.put(currentBytecode,
								new LineAnnotation(AnnotationType.BYTECODE, reason.toString(), Color.GREEN));
					}
						break;
					case TAG_INLINE_FAIL:
					{
						StringBuilder reason = new StringBuilder();
						reason.append("Not inlined: ").append(attrs.get(ATTR_REASON));
						
						if (callAttrs.containsKey(ATTR_COUNT))
						{
							reason.append("\nCount: ").append(callAttrs.get(ATTR_COUNT));
						}
						if (callAttrs.containsKey(ATTR_PROF_FACTOR))
						{
							reason.append("\nProf factor: ").append(callAttrs.get(ATTR_PROF_FACTOR));
						}
						
						result.put(currentBytecode,
								new LineAnnotation(AnnotationType.BYTECODE, reason.toString(), Color.RED));
					}
						break;
					case TAG_BRANCH:
					{
						String count = attrs.get(ATTR_BRANCH_COUNT);
						String taken = attrs.get(ATTR_BRANCH_TAKEN);
						String notTaken = attrs.get(ATTR_BRANCH_NOT_TAKEN);
						String prob = attrs.get(ATTR_BRANCH_PROB);

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
							result.put(currentBytecode, new LineAnnotation(AnnotationType.BYTECODE, reason.toString(), Color.BLUE));
						}
					}
						break;
					case TAG_INTRINSIC:
					{
						StringBuilder reason = new StringBuilder();
						reason.append("Intrinsic: ").append(attrs.get(ATTR_ID));

						result.put(currentBytecode, new LineAnnotation(AnnotationType.BYTECODE, reason.toString(), Color.GREEN));

					}
						break;
					}
				}
			}
		}

		return result;
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

	public static List<Tag> getParseTags(Journal journal)
	{
		List<Tag> result = new ArrayList<>();

		Task lastTask = getLastTask(journal);

		if (lastTask != null)
		{
			List<Tag> parsePhases = lastTask.getNamedChildrenWithAttribute(TAG_PHASE, ATTR_NAME, ATTR_PARSE);
			
			System.out.println("Parse phases: " + parsePhases.size());

			for (Tag parsePhase : parsePhases)
			{
				result.addAll(parsePhase.getNamedChildren(TAG_PARSE));
			}
		}

		return result;
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
