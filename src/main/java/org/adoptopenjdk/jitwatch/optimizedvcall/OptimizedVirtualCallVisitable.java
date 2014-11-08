package org.adoptopenjdk.jitwatch.optimizedvcall;

import java.util.List;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.treevisitor.ITreeVisitable;

public interface OptimizedVirtualCallVisitable extends ITreeVisitable
{
	List<IMetaMember> buildOptimizedCalleeReport();
}