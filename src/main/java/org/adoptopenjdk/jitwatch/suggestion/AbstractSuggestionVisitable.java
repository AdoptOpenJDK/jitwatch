package org.adoptopenjdk.jitwatch.suggestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.treevisitor.ITreeVisitable;
import org.adoptopenjdk.jitwatch.treevisitor.TreeVisitor;

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