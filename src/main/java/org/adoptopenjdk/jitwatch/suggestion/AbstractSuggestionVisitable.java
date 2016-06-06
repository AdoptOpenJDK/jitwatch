/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.suggestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.adoptopenjdk.jitwatch.journal.AbstractJournalVisitable;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.treevisitor.ITreeVisitable;
import org.adoptopenjdk.jitwatch.treevisitor.TreeVisitor;

public abstract class AbstractSuggestionVisitable extends AbstractJournalVisitable implements ITreeVisitable
{
    protected IReadOnlyJITDataModel model;
    protected List<Suggestion> suggestionList;

	public AbstractSuggestionVisitable(IReadOnlyJITDataModel model)
	{
		this.model = model;
		suggestionList = new ArrayList<>();
	}

	public List<Suggestion> getSuggestionList()
	{
		TreeVisitor.walkTree(model, this);
		
		findNonMemberSuggestions();

		Collections.sort(suggestionList, new Comparator<Suggestion>()
		{
			@Override
			public int compare(Suggestion s1, Suggestion s2)
			{
				return Integer.compare(s2.getScore(), s1.getScore());
			}
		});

		return suggestionList;
	}
	
	protected abstract void findNonMemberSuggestions();


	@Override
	public void reset()
	{
		suggestionList.clear();
	}
}