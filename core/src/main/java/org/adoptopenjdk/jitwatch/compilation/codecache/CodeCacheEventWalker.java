/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.compilation.codecache;

import org.adoptopenjdk.jitwatch.compilation.AbstractCompilationWalker;
import org.adoptopenjdk.jitwatch.model.CodeCacheEvent;
import org.adoptopenjdk.jitwatch.model.CodeCacheEvent.CodeCacheEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;

public class CodeCacheEventWalker extends AbstractCompilationWalker
{
	private CodeCacheWalkerResult result = new CodeCacheWalkerResult();

	private static final Logger logger = LoggerFactory.getLogger(CodeCacheEventWalker.class);
	
	public CodeCacheEventWalker(IReadOnlyJITDataModel model)
	{
		super(model);
	}

	@Override
	public void reset()
	{
		result.reset();
	}

	@Override
	public void visit(IMetaMember metaMember)
	{
		if (metaMember != null && metaMember.isCompiled())
		{
			for (Compilation compilation : metaMember.getCompilations())
			{
				if (compilation.isFailedTask())
				{
					continue;
				}
				
				String address = compilation.getNativeAddress(); // hex string

				long addressLong = 0;

				long stamp = compilation.getCompiledStamp();

				try
				{
					addressLong = Long.decode(address);
				}
				catch (NullPointerException | NumberFormatException exception)
				{
					
					logger.error("Couldn't decode address {} on compilation {}", address, compilation);
					continue; // don't allow a zero address
				}

				// intrinsic has no size info
				int nativeCodeSize = compilation.getNativeSize();

				CodeCacheEvent event = new CodeCacheEvent(CodeCacheEventType.COMPILATION, stamp, nativeCodeSize, 0);
				event.setNativeAddress(addressLong);
				event.setCompilation(compilation);

				result.addEvent(event);
			}
		}
	}

	public CodeCacheWalkerResult getResult()
	{
		return result;
	}
}