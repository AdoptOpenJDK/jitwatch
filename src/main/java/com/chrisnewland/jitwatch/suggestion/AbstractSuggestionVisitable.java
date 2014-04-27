package com.chrisnewland.jitwatch.suggestion;

import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.treevisitor.ITreeVisitable;
import com.chrisnewland.jitwatch.treevisitor.TreeVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractSuggestionVisitable implements ITreeVisitable
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
		
		Collections.sort(suggestionList, new Comparator<Suggestion>()
		{
			@Override
			public int compare(Suggestion s1, Suggestion s2)
			{
				return Integer.compare(s1.getScore(), s2.getScore());
			}
		});
		
		return suggestionList;
	}
	
	@Override
	public void reset()
	{		
		suggestionList.clear();
	}
}