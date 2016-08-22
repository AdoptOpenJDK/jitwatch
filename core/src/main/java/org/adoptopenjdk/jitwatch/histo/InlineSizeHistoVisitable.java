/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.histo;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BYTES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_HOLDER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_PARSE_HIR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BRANCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_UNCOMMON_TRAP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DEPENDENCY;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DIRECT_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PREDICTED_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OBSERVE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_VIRTUAL_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CAST_UP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ASSERT_NULL;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.adoptopenjdk.jitwatch.compilation.CompilationUtil;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InlineSizeHistoVisitable extends AbstractHistoVisitable
{
	private static final Logger logger = LoggerFactory.getLogger(InlineSizeHistoVisitable.class);

	private Set<String> inlinedCounted = new HashSet<>();

	public InlineSizeHistoVisitable(IReadOnlyJITDataModel model, long resolution)
	{
		super(model, resolution);
		
		ignoreTags.add(TAG_CALL);
		ignoreTags.add(TAG_DEPENDENCY);	
		ignoreTags.add(TAG_BC);
		ignoreTags.add(TAG_KLASS);
		ignoreTags.add(TAG_PARSE_DONE);
		ignoreTags.add(TAG_UNCOMMON_TRAP);
		ignoreTags.add(TAG_TYPE);
		ignoreTags.add(TAG_BRANCH);
		ignoreTags.add(TAG_DIRECT_CALL);
		ignoreTags.add(TAG_PREDICTED_CALL);
		ignoreTags.add(TAG_OBSERVE);
		ignoreTags.add(TAG_VIRTUAL_CALL);
		ignoreTags.add(TAG_INTRINSIC);
		ignoreTags.add(TAG_CAST_UP);
		ignoreTags.add(TAG_ASSERT_NULL);
	}

	@Override
	public void reset()
	{
		inlinedCounted.clear();
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
				logger.error("Could not build histo for {}", metaMember.getMemberName(), e);
			}
		}
	}

	private void processParseTag(Tag parseTag, IParseDictionary parseDictionary)
	{
		String currentMethod = null;
		String holder = null;
		String attrInlineBytes = null;

		for (Tag child : parseTag.getChildren())
		{
			String tagName = child.getName();
			Map<String, String> attrs = child.getAttributes();

			switch (tagName)
			{
			case TAG_METHOD:
			{
				currentMethod = attrs.get(ATTR_NAME);
				holder = attrs.get(ATTR_HOLDER);
				attrInlineBytes = attrs.get(ATTR_BYTES);
				break;
			}

			case TAG_INLINE_FAIL:
			{
				// clear method to prevent incorrect pickup by next inline
				// success
				currentMethod = null;
				holder = null;
				attrInlineBytes = null;
				
				break;
			}

			case TAG_INLINE_SUCCESS:
			{
				if (holder != null && currentMethod != null && attrInlineBytes != null)
				{
					Tag klassTag = parseDictionary.getKlass(holder);

					if (klassTag != null)
					{
						String fqName = klassTag.getAttributes().get(ATTR_NAME) + C_SLASH + currentMethod;

						if (!inlinedCounted.contains(fqName))
						{
							long inlinedByteCount = Long.parseLong(attrInlineBytes);
							histo.addValue(inlinedByteCount);

							inlinedCounted.add(fqName);
						}
					}
				}
				
				break;
			}
				
			case TAG_PARSE:
			{
				processParseTag(child, parseDictionary);
				break;
			}
				
  			case TAG_PHASE:
			{
				String phaseName = attrs.get(ATTR_NAME);
				
				if (S_PARSE_HIR.equals(phaseName))
				{
					processParseTag(child, parseDictionary);
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
	public void visitTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
		processParseTag(parseTag, parseDictionary);
	}
}