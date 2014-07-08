package org.adoptopenjdk.jitwatch.histo;

import org.adoptopenjdk.jitwatch.treevisitor.ITreeVisitable;

public interface IHistoVisitable extends ITreeVisitable
{
	Histo buildHistogram();
}