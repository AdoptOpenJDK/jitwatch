package org.adoptopenjdk.jitwatch.histo;

import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.treevisitor.TreeVisitor;

public abstract class AbstractHistoVisitable implements IHistoVisitable
{
    protected Histo histo;
    protected IReadOnlyJITDataModel model;
    protected long resolution;
	
	public AbstractHistoVisitable(IReadOnlyJITDataModel model, long resolution)
	{
		this.model = model;
		this.resolution = resolution;
	}
	
	public Histo buildHistogram()
	{
		histo = new Histo(resolution);
				
		TreeVisitor.walkTree(model, this);
		
		return histo;
	}
	
	public void reset()
	{		
	}
}