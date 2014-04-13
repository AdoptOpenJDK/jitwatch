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

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class CompileChainWalker
{
	private IParseDictionary parseDictionary;

	private IReadOnlyJITDataModel model;

	public CompileChainWalker(IReadOnlyJITDataModel model)
	{
		this.model = model;
	}

	public CompileNode buildCallTree(IMetaMember mm)
	{
		CompileNode root = null;

		if (mm.isCompiled())
		{
			Journal journal = mm.getJournal();

			Task lastTaskTag = JournalUtil.getLastTask(journal);

			if (lastTaskTag != null)
			{
				parseDictionary = lastTaskTag.getParseDictionary();

				Tag parsePhase = JournalUtil.getParsePhase(journal);

				// TODO fix for JDK8
				if (parsePhase != null)
				{
					List<Tag> parseTags = parsePhase.getNamedChildren(TAG_PARSE);

					for (Tag parseTag : parseTags)
					{
						String id = parseTag.getAttribute(ATTR_METHOD);

						root = new CompileNode(mm, id);

						processParseTag(parseTag, root);
					}
				}
			}
		}

		return root;
	}

	public String buildCompileChainTextRepresentation(CompileNode node)
	{
		StringBuilder builder = new StringBuilder();

		show(node, builder, 0);

		return builder.toString();
	}

	private void show(CompileNode node, StringBuilder builder, int depth)
	{
		if (depth > 0)
		{
			for (int i = 0; i < depth; i++)
			{
				builder.append("\t");
			}

			builder.append(" -> ");

			builder.append(node.getMember().getMemberName());

			builder.append("[");

			if (node.getMember().isCompiled())
			{
				builder.append("C");
			}

			if (node.isInlined())
			{
				builder.append("I");
			}

			builder.append("]");

			if (depth == 0)
			{
				builder.append("\n");
			}
		}

		for (CompileNode child : node.getChildren())
		{
			show(child, builder, depth + 1);
		}

		if (node.getChildren().size() == 0)
		{
			builder.append("\n");
		}
	}

	private void processParseTag(Tag parseTag, CompileNode node)
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

				CompileNode childNode = new CompileNode(childCall, methodID);
				node.addChild(childNode);

				String reason = tagAttrs.get(ATTR_REASON);
				String annotationText = InlineUtil.buildInlineAnnotationText(false, reason, callAttrs, methodAttrs);
				childNode.setInlined(inlined, annotationText);

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

				CompileNode childNode = new CompileNode(childCall, childMethodID);
				node.addChild(childNode);

				if (methodID != null && methodID.equals(childMethodID))
				{
					childNode.setInlined(inlined, inlineReason);
				}

				processParseTag(child, childNode);
			}

            default:
                break;
			}
		}
	}
}