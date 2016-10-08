/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.hotthrow;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_HOLDER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_PARSE_HIR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ASSERT_NULL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BRANCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CAST_UP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DEPENDENCY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DIRECT_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_PREALLOCATED;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OBSERVE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PREDICTED_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_UNCOMMON_TRAP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_HOT_THROW;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BCI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adoptopenjdk.jitwatch.compilation.AbstractCompilationVisitable;
import org.adoptopenjdk.jitwatch.compilation.CompilationUtil;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.bytecode.ExceptionTable;
import org.adoptopenjdk.jitwatch.model.bytecode.ExceptionTableEntry;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HotThrowFinder extends AbstractCompilationVisitable
{
	private Set<HotThrowResult> result;
	private IReadOnlyJITDataModel model;

	private static final Logger logger = LoggerFactory.getLogger(HotThrowFinder.class);

	public HotThrowFinder(IReadOnlyJITDataModel model)
	{
		this.model = model;

		ignoreTags.add(TAG_KLASS);
		ignoreTags.add(TAG_TYPE);
		ignoreTags.add(TAG_UNCOMMON_TRAP);
		ignoreTags.add(TAG_PARSE_DONE);
		ignoreTags.add(TAG_BRANCH);
		ignoreTags.add(TAG_CAST_UP);
		ignoreTags.add(TAG_PARSE);
		ignoreTags.add(TAG_INLINE_SUCCESS);
		ignoreTags.add(TAG_INLINE_FAIL);
		ignoreTags.add(TAG_DIRECT_CALL);
		ignoreTags.add(TAG_PREDICTED_CALL);
		ignoreTags.add(TAG_DEPENDENCY);
		ignoreTags.add(TAG_OBSERVE);
		ignoreTags.add(TAG_INTRINSIC);
		ignoreTags.add(TAG_ASSERT_NULL);
	}

	public Set<HotThrowResult> findHotThrows(IMetaMember member)
	{
		result = new HashSet<>();

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
				logger.error("Error while finding hot throws for member {}", member, e);
			}
		}

		return result;
	}

	@Override
	public void visitTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
		String currentMethod = null;
		String holder = null;
		String currentBCI = null;

		Map<String, String> attrs = parseTag.getAttributes();

		String methodID = attrs.get(ATTR_METHOD);

		Tag methodTag = parseDictionary.getMethod(methodID);

		Map<String, String> methodTagAttributes = methodTag.getAttributes();

		currentMethod = methodTagAttributes.get(ATTR_NAME);
		holder = methodTagAttributes.get(ATTR_HOLDER);

		List<Tag> allChildren = parseTag.getChildren();

		for (Tag child : allChildren)
		{
			String tagName = child.getName();
			attrs = child.getAttributes();

			switch (tagName)
			{
			case TAG_METHOD:
			{
				currentMethod = attrs.get(ATTR_NAME);
				holder = attrs.get(ATTR_HOLDER);
				break;
			}
			case TAG_BC:
			{
				currentBCI = attrs.get(ATTR_BCI);
				break;
			}

			// changes member context
			case TAG_CALL:
			{
				methodID = attrs.get(ATTR_METHOD);

				methodTag = parseDictionary.getMethod(methodID);

				methodTagAttributes = methodTag.getAttributes();

				currentMethod = methodTagAttributes.get(ATTR_NAME);
				holder = methodTagAttributes.get(ATTR_HOLDER);
				break;
			}

			case TAG_HOT_THROW:
			{
				if (holder != null && currentMethod != null)
				{
					Tag klassTag = parseDictionary.getKlass(holder);

					String preallocated = child.getAttributes().get(ATTR_PREALLOCATED);

					if (currentBCI != null && klassTag != null)
					{
						IMetaMember member = ParseUtil.lookupMember(methodID, parseDictionary, model);

						if (member != null)
						{
							MemberBytecode memberBytecode = member.getMemberBytecode();

							if (memberBytecode == null)
							{
								member.getMetaClass().getClassBytecode(model, new ArrayList<String>());

								memberBytecode = member.getMemberBytecode();
							}

							if (memberBytecode != null)
							{
								ExceptionTable exceptionTable = memberBytecode.getExceptionTable();

								if (exceptionTable != null)
								{
									int bciValue = Integer.valueOf(currentBCI);
									
									ExceptionTableEntry entry = exceptionTable.getEntryForBCI(bciValue);

									if (entry != null)
									{
										HotThrowResult throwResult = new HotThrowResult(member, bciValue, entry.getType(), "1".equals(preallocated));

										result.add(throwResult);
									}
								}
							}
						}
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