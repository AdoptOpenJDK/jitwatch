package com.chrisnewland.jitwatch.histo;

import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;

public abstract class AbstractHistoTreeWalker implements IHistoWalker
{
	protected long resolution;
	protected IReadOnlyJITDataModel model;
	
	public AbstractHistoTreeWalker(IReadOnlyJITDataModel model, long resolution)
	{
		this.model = model;
		this.resolution = resolution;
	}

	@Override
	public long getResolution()
	{
		return resolution;
	}
	
	@Override
	public IReadOnlyJITDataModel getJITDataModel()
	{
		return model;
	}
	
	@Override
	public void reset()
	{
		
	}
}
