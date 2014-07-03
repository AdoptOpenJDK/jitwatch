/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.chain;

import com.chrisnewland.jitwatch.model.*;
import com.chrisnewland.jitwatch.util.InlineUtil;
import com.chrisnewland.jitwatch.util.JournalUtil;
import com.chrisnewland.jitwatch.util.ParseUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class CompileChainWalker
{
	private static final Logger logger = LoggerFactory.getLogger(CompileChainWalker.class);

	private IParseDictionary parseDictionary;

	private IReadOnlyJITDataModel model;

	public CompileChainWalker(IReadOnlyJITDataModel model)
	{
		this.model = model;
	}

	public CompileNode buildCallTree(IMetaMember mm)
	{
		logger.info("buildCallTree: {}", mm.toStringUnqualifiedMethodName(false));

		CompileNode root = null;

		Journal journal = mm.getJournal();

		Task lastTaskTag = JournalUtil.getLastTask(journal);

		if (lastTaskTag != null)
		{
			parseDictionary = lastTaskTag.getParseDictionary();

			Tag parsePhase = JournalUtil.getParsePhase(journal);

			if (parsePhase != null)
			{
				List<Tag> parseTags = parsePhase.getNamedChildren(TAG_PARSE);

				for (Tag parseTag : parseTags)
				{
					String id = parseTag.getAttribute(ATTR_METHOD);

					// only initialise on first parse tag.
					// there may be multiple if late_inline
					// is detected
					if (root == null)
					{
						root = new CompileNode(mm, id);
					}

					processParseTag(parseTag, root);
				}
			}
		}

		return root;
	}

	private void processParseTag(Tag parseTag, CompileNode parentNode)
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

					processParseTag(child, childNode);
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
}