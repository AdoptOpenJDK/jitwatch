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

import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.LineAnnotation;
import com.chrisnewland.jitwatch.model.LineAnnotation.AnnotationType;
import com.chrisnewland.jitwatch.model.Tag;

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

				for (Tag child : children)
				{
					String name = child.getName();
					Map<String, String> attrs = child.getAttrs();

					switch (name)
					{
					case TAG_BC:
						String bci = attrs.get(ATTR_BCI);
						currentBytecode = Integer.parseInt(bci);
						break;
					case TAG_INLINE_SUCCESS:
						result.put(currentBytecode, new LineAnnotation(AnnotationType.BYTECODE, "Inlined: " + attrs.get(ATTR_REASON),
								Color.GREEN));
						break;
					case TAG_INLINE_FAIL:
						result.put(currentBytecode, new LineAnnotation(AnnotationType.BYTECODE, "Not Inlined: " + attrs.get(ATTR_REASON),
								Color.RED));
						break;
					case TAG_BRANCH:
					
						String count = attrs.get(ATTR_BRANCH_COUNT);
						String taken = attrs.get(ATTR_BRANCH_TAKEN);
						String prob = attrs.get(ATTR_BRANCH_PROB);

						StringBuilder reason = new StringBuilder();
						reason.append("Branch taken ").append(taken).append("/").append(count).append(". Probability:")
								.append(prob);

						result.put(currentBytecode, new LineAnnotation(AnnotationType.BYTECODE, reason.toString(), Color.BLUE));
					
						break;
					}
				}
			}
		}

		return result;
	}

	private static List<Tag> getParseTags(Journal journal)
	{
		List<Tag> result = new ArrayList<>();

		Tag lastTaskTag = null;

		for (Tag tag : journal.getEntryList())
		{
			if (TAG_TASK.equals(tag.getName()))
			{
				lastTaskTag = tag;
			}
		}

		if (lastTaskTag != null)
		{
			List<Tag> parsePhases = lastTaskTag.getNamedChildrenWithAttribute(TAG_PHASE, ATTR_NAME, ATTR_PARSE);

			for (Tag parsePhase : parsePhases)
			{
				result.addAll(parsePhase.getNamedChildren(TAG_PARSE));
			}
		}

		return result;

	}
}
