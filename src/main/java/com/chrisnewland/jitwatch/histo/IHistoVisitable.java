package com.chrisnewland.jitwatch.histo;

import com.chrisnewland.jitwatch.treevisitor.ITreeVisitable;

public interface IHistoVisitable extends ITreeVisitable
{
	public Histo buildHistogram();
}