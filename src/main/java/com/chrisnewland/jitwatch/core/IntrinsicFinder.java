package com.chrisnewland.jitwatch.core;

import java.util.HashMap;

import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.Tag;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class IntrinsicFinder
{
	public static Map<String, String> findIntrinsics(Journal journal)
	{
		Map<String, String> result = new HashMap<>();

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

			if (parsePhases.size() > 0)
			{
				Tag lastParsePhase = parsePhases.get(parsePhases.size() - 1);

				List<Tag> parseTags = lastParsePhase.getNamedChildren(ATTR_PARSE);

				for (Tag parseTag : parseTags)
				{
					String klass = null;
					String method = null;

					// <klass>
					// <method>
					// <intrinsic>
					List<Tag> allChildren = parseTag.getChildren();

					for (Tag childTag : allChildren)
					{
						String name = childTag.getName();

						switch (name)
						{
						case TAG_KLASS:
							klass = childTag.getAttrs().get(ATTR_NAME);
							klass = klass.replace("/", ".");
							break;

						case TAG_METHOD:
							method = childTag.getAttrs().get(ATTR_NAME);
							break;

						case TAG_INTRINSIC:
							String intrinsic = childTag.getAttrs().get(ATTR_ID);

							if (klass != null && method != null)
							{
								result.put(klass + "." + method, intrinsic);
							}

							klass = null;
							method = null;
							break;
						}
					}
				}

			}

		}

		return result;
	}
}
