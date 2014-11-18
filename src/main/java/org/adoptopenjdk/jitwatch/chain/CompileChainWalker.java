/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.chain;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_REASON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.journal.ILastTaskParseTagVisitable;
import org.adoptopenjdk.jitwatch.journal.JournalUtil;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.util.InlineUtil;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompileChainWalker implements ILastTaskParseTagVisitable
{
	private static final Logger logger = LoggerFactory.getLogger(CompileChainWalker.class);

	private IReadOnlyJITDataModel model;

	private CompileNode root = null;

	private IMetaMember metaMember = null;

	public CompileChainWalker(IReadOnlyJITDataModel model)
	{
		this.model = model;
	}

	public CompileNode buildCallTree(IMetaMember metaMember)
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("buildCallTree: {}", metaMember.toStringUnqualifiedMethodName(false));
		}

		this.root = null;
		this.metaMember = metaMember;

		try
		{
			JournalUtil.visitParseTagsOfLastTask(metaMember, this);
		}
		catch (LogParseException lpe)
		{
			logger.error("Could not build compile tree", lpe);
		}

		return root;
	}

	private void processParseTag(Tag parseTag, CompileNode parentNode, IParseDictionary parseDictionary)
	{
		String methodID = null;
		boolean inlined = false;
		String inlineReason = null;

		Map<String, String> methodAttrs = new HashMap<>();
		Map<String, String> callAttrs = new HashMap<>();

		// TODO - this switch code is appearing a lot
		// should probably refactor with an interface
		// or visitor pattern
		for (Tag child : parseTag.getChildren())
		{
			String tagName = child.getName();
			Map<String, String> tagAttrs = child.getAttrs();

			switch (tagName)
			{
			case TAG_BC:
			{
				callAttrs.clear();
			}
				break;

			case TAG_METHOD:
			{
				methodID = tagAttrs.get(ATTR_ID);
				inlined = false; // reset
				methodAttrs.clear();
				methodAttrs.putAll(tagAttrs);
			}
				break;

			case TAG_CALL:
			{
				methodID = tagAttrs.get(ATTR_METHOD);
				inlined = false;
				callAttrs.clear();
				callAttrs.putAll(tagAttrs);
			}
				break;

			case TAG_INLINE_FAIL:
			{
				inlined = false; // reset

				IMetaMember childCall = ParseUtil.lookupMember(methodID, parseDictionary, model);

				if (childCall != null)
				{
					CompileNode childNode = new CompileNode(childCall, methodID);
					parentNode.addChild(childNode);

					String reason = tagAttrs.get(ATTR_REASON);
					String annotationText = InlineUtil.buildInlineAnnotationText(false, reason, callAttrs, methodAttrs);
					childNode.setInlined(inlined, annotationText);
				}
				else
				{
					logger.error("TAG_INLINE_FAIL Failed to create CompileNode with null member. Method was {}", methodID);
				}

				methodID = null;
			}
				break;

			case TAG_INLINE_SUCCESS:
				inlined = true;
				String reason = tagAttrs.get(ATTR_REASON);
				inlineReason = InlineUtil.buildInlineAnnotationText(true, reason, callAttrs, methodAttrs);
				break;

			case TAG_PARSE: // call depth
			{
				String childMethodID = tagAttrs.get(ATTR_METHOD);

				IMetaMember childCall = ParseUtil.lookupMember(childMethodID, parseDictionary, model);

				if (childCall != null)
				{
					CompileNode childNode = new CompileNode(childCall, childMethodID);

					parentNode.addChild(childNode);

					if (methodID != null && methodID.equals(childMethodID))
					{
						childNode.setInlined(inlined, inlineReason);
					}

					processParseTag(child, childNode, parseDictionary);
				}
				else
				{
					logger.error("TAG_PARSE Failed to create CompileNode with null member. Method was {}", childMethodID);
				}
			}
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void visitParseTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
		String id = parseTag.getAttribute(ATTR_METHOD);

		// only initialise on first parse tag.
		// there may be multiple if late_inline
		// is detected
		if (root == null)
		{
			root = new CompileNode(metaMember, id);
		}

		processParseTag(parseTag, root, parseDictionary);
	}
}