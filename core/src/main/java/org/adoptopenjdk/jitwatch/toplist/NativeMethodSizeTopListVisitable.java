/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BRANCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CAST_UP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DEPENDENCY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DIRECT_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PREDICTED_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_UNCOMMON_TRAP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;


import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;

public class NativeMethodSizeTopListVisitable extends AbstractTopListVisitable
{
	public NativeMethodSizeTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
		
		ignoreTags.add(TAG_BC);
		ignoreTags.add(TAG_KLASS);
		ignoreTags.add(TAG_TYPE);
		ignoreTags.add(TAG_METHOD);
		ignoreTags.add(TAG_CALL);
		ignoreTags.add(TAG_INTRINSIC);
		ignoreTags.add(TAG_UNCOMMON_TRAP);
		ignoreTags.add(TAG_PARSE_DONE);
		ignoreTags.add(TAG_BRANCH);
		ignoreTags.add(TAG_CAST_UP);
		ignoreTags.add(TAG_INLINE_SUCCESS);
		ignoreTags.add(TAG_DIRECT_CALL);
		ignoreTags.add(TAG_PREDICTED_CALL);
		ignoreTags.add(TAG_DEPENDENCY);	
	}

	@Override
	public void visit(IMetaMember metaMember)
	{		
		for (Compilation compilation : metaMember.getCompilations())
		{
			long nativeSize = compilation.getNativeSize();
	
			if (nativeSize != 0)
			{
				topList.add(new MemberScore(metaMember, nativeSize));
			}
		}
	}
}