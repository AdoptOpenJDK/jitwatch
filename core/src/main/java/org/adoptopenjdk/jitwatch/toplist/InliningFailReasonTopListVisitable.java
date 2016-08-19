/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_REASON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_PARSE_HIR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BRANCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CAST_UP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DEPENDENCY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DIRECT_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PREDICTED_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_UNCOMMON_TRAP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;


import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.compilation.CompilationUtil;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class InliningFailReasonTopListVisitable extends AbstractTopListVisitable
{
	private final Map<String, Integer> reasonCountMap;

	public InliningFailReasonTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
		reasonCountMap = new HashMap<>();
		
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
		if (metaMember != null && metaMember.isCompiled())
		{
			try
			{
				for (Compilation compilation : metaMember.getCompilations())
				{
					CompilationUtil.visitParseTagsOfCompilation(compilation, this);
				}   
			}
			catch (LogParseException e)
			{
				logger.error("Error building inlining stats", e);
			}
		}
	}

	private void processParseTag(Tag parseTag)
	{
		for (Tag child : parseTag.getChildren())
		{
			String tagName = child.getName();
			Map<String, String> attrs = child.getAttributes();
			
			switch (tagName)
			{
			case TAG_INLINE_FAIL:
			{
				String reason = attrs.get(ATTR_REASON);
				
				reason = StringUtil.replaceXMLEntities(reason);						

				if (reasonCountMap.containsKey(reason))
				{
					int count = reasonCountMap.get(reason);
					reasonCountMap.put(reason, count + 1);
				}
				else
				{
					reasonCountMap.put(reason, 1);
				}
				
				break;
			}
			case TAG_PARSE:
			{
				processParseTag(child);
				break;
			}
			
  			case TAG_PHASE:
			{
				String phaseName = attrs.get(ATTR_NAME);
				
				if (S_PARSE_HIR.equals(phaseName))
				{
					processParseTag(child);
				}
				else
				{
					logger.warn("Don't know how to handle phase {}", phaseName);
				}
				
				break;
			}
			
			default:
				handleOther(child);
				break;
			}
		}
	}

	@Override
	public void postProcess()
	{
		for (Map.Entry<String, Integer> entry : reasonCountMap.entrySet())
		{
			topList.add(new StringTopListScore(entry.getKey(), entry.getValue().longValue()));
		}
	}

	@Override
	public void visitTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
		processParseTag(parseTag);
	}
}