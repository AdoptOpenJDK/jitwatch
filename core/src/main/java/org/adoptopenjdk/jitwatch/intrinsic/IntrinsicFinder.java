/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.intrinsic;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_HOLDER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_PARSE_HIR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BRANCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CAST_UP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DEPENDENCY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DIRECT_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PREDICTED_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_UNCOMMON_TRAP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ASSERT_NULL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OBSERVE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_HOT_THROW;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_VIRTUAL_CALL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.compilation.AbstractCompilationVisitable;
import org.adoptopenjdk.jitwatch.compilation.CompilationUtil;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IntrinsicFinder extends AbstractCompilationVisitable
{
	private Map<String, String> result;

	private static final Logger logger = LoggerFactory.getLogger(IntrinsicFinder.class);

	public IntrinsicFinder()
	{
		ignoreTags.add(TAG_BC);
		ignoreTags.add(TAG_KLASS);
		ignoreTags.add(TAG_TYPE);
		ignoreTags.add(TAG_UNCOMMON_TRAP);
		ignoreTags.add(TAG_PARSE_DONE);
		ignoreTags.add(TAG_BRANCH);
		ignoreTags.add(TAG_CAST_UP);
		ignoreTags.add(TAG_INLINE_SUCCESS);
		ignoreTags.add(TAG_INLINE_FAIL);
		ignoreTags.add(TAG_DIRECT_CALL);
		ignoreTags.add(TAG_PREDICTED_CALL);
		ignoreTags.add(TAG_DEPENDENCY);
		ignoreTags.add(TAG_ASSERT_NULL);	
		ignoreTags.add(TAG_OBSERVE);	
		ignoreTags.add(TAG_HOT_THROW);
		ignoreTags.add(TAG_VIRTUAL_CALL);
	}

	public Map<String, String> findIntrinsics(IMetaMember member)
	{
		result = new HashMap<>();

		if (member != null)
		{
			try
			{
				for (Compilation compilation : member.getCompilations())
				{
					CompilationUtil.visitParseTagsOfCompilation(compilation, this);
				}   
			}
			catch (LogParseException e)
			{
				logger.error("Error while finding intrinsics for member {}", member, e);
			}
		}

		return result;
	}

	@Override
	public void visitTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
		String currentMethod = null;
		String holder = null;

		List<Tag> allChildren = parseTag.getChildren();

		for (Tag child : allChildren)
		{
			String tagName = child.getName();
			Map<String, String> attrs = child.getAttributes();

			switch (tagName)
			{
			case TAG_METHOD:
			{
				currentMethod = attrs.get(ATTR_NAME);
				holder = attrs.get(ATTR_HOLDER);
				break;
			}

				// changes member context
			case TAG_CALL:
			{
				String methodID = attrs.get(ATTR_METHOD);

				Tag methodTag = parseDictionary.getMethod(methodID);
				
				Map<String, String> methodTagAttributes = methodTag.getAttributes();
				
				currentMethod = methodTagAttributes.get(ATTR_NAME);
				holder = methodTagAttributes.get(ATTR_HOLDER);
				break;
			}

			case TAG_INTRINSIC:
			{
				if (holder != null && currentMethod != null)
				{
					Tag klassTag = parseDictionary.getKlass(holder);

					String intrinsic = child.getAttributes().get(ATTR_ID);

					if (klassTag != null)
					{
						String fqName = klassTag.getAttributes().get(ATTR_NAME).replace(C_SLASH, C_DOT) + C_DOT + currentMethod;

						result.put(fqName, intrinsic);
					}
				}

				holder = null;
				currentMethod = null;
				break;
			}

			case TAG_PHASE:
			{
				String phaseName = attrs.get(ATTR_NAME);

				if (S_PARSE_HIR.equals(phaseName))
				{
					visitTag(child, parseDictionary);
				}
				else
				{
					logger.warn("Don't know how to handle phase {}", phaseName);
				}

				break;
			}
			
			case TAG_PARSE: // nested parse from inlining
			{
				visitTag(child, parseDictionary);
				break;
			}

			default:
				handleOther(child);
				break;
			}
		}
	}
}
