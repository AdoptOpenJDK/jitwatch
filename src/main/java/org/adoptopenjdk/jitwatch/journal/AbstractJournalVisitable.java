package org.adoptopenjdk.jitwatch.journal;

import java.util.HashSet;
import java.util.Set;

import org.adoptopenjdk.jitwatch.model.Tag;

public abstract class AbstractJournalVisitable implements IJournalVisitable
{	
	protected Set<String> ignoreTags = new HashSet<>();

	protected void handleOther(Tag tag)
	{		
		if (!ignoreTags.contains(tag.getName()))
		{
			JournalUtil.unhandledTag(this, tag);
		}
	}
}