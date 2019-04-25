/*
 * Copyright (c) 2013-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.chain;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.compilation.AbstractCompilationVisitable;
import org.adoptopenjdk.jitwatch.compilation.CompilationUtil;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.util.TooltipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class CompileChainWalker extends AbstractCompilationVisitable
{
	private static final Logger logger = LoggerFactory.getLogger(CompileChainWalker.class);

	private IReadOnlyJITDataModel model;

	private CompileNode root = null;

	public CompileChainWalker(IReadOnlyJITDataModel model)
	{
		this.model = model;

		ignoreTags.add(TAG_DIRECT_CALL);
		ignoreTags.add(TAG_KLASS);
		ignoreTags.add(TAG_TYPE);
		ignoreTags.add(TAG_DEPENDENCY);
		ignoreTags.add(TAG_PREDICTED_CALL);
		ignoreTags.add(TAG_PARSE_DONE);
		ignoreTags.add(TAG_PHASE_DONE);
		ignoreTags.add(TAG_BRANCH);
		ignoreTags.add(TAG_UNCOMMON_TRAP);
		ignoreTags.add(TAG_INTRINSIC);
		ignoreTags.add(TAG_OBSERVE);
		ignoreTags.add(TAG_CAST_UP);
		ignoreTags.add(TAG_HOT_THROW);
	}

	public void clear()
	{
		root = null;
	}

	public CompileNode buildCallTree(Compilation compilation)
	{
		this.root = null;

		if (compilation != null)
		{
			try
			{
				CompilationUtil.visitParseTagsOfCompilation(compilation, this);
			}
			catch (LogParseException lpe)
			{
				logger.error("Could not build compile tree", lpe);
			}
		}

		return root;
	}

	private void processParseTag(Tag parseTag, CompileNode parentNode, IParseDictionary parseDictionary)
	{
		String methodID = null;
		CompileNode lastNode = null;

		String callerBCI = null;

		Map<String, String> methodAttrs = new HashMap<>();
		Map<String, String> callAttrs = new HashMap<>();

		for (Tag child : parseTag.getChildren())
		{
			String tagName = child.getName();
			Map<String, String> tagAttrs = child.getAttributes();

			switch (tagName)
			{
			case TAG_BC:
			{
				callAttrs.clear();
				callerBCI = tagAttrs.get(ATTR_BCI);
				break;
			}

			case TAG_METHOD:
			{
				methodID = tagAttrs.get(ATTR_ID);
				methodAttrs.clear();
				methodAttrs.putAll(tagAttrs);
				break;
			}

			case TAG_CALL:
			{
				methodID = tagAttrs.get(ATTR_METHOD);
				callAttrs.clear();
				callAttrs.putAll(tagAttrs);
				break;
			}

			case TAG_INLINE_FAIL:
			{
				createChildNode(parentNode, callerBCI, methodID, parseDictionary, false, false, methodAttrs, callAttrs, tagAttrs);
				methodID = null;
				lastNode = null;
				break;
			}

			case TAG_INLINE_SUCCESS:
			{
				lastNode = createChildNode(parentNode, callerBCI, methodID, parseDictionary, true, false, methodAttrs, callAttrs,
						tagAttrs);
				break;
			}

			case TAG_PARSE: // call depth
			{
				String childMethodID = tagAttrs.get(ATTR_METHOD);

				CompileNode nextParent = parentNode;

				if (lastNode != null)
				{
					nextParent = lastNode;
				}
				else if (!child.getNamedChildren(TAG_PARSE).isEmpty())
				{
					CompileNode childNode = new CompileNode(childMethodID);

					parentNode.addChild(childNode);

					nextParent = childNode;
				}

				processParseTag(child, nextParent, parseDictionary);

				break;
			}

			case TAG_PHASE:
			{
				String phaseName = tagAttrs.get(ATTR_NAME);

				if (S_PARSE_HIR.equals(phaseName))
				{
					processParseTag(child, parentNode, parseDictionary);
				}
				else
				{
					logger.warn("Don't know how to handle phase {}", phaseName);
				}
				break;
			}

			case TAG_VIRTUAL_CALL:
				lastNode = createChildNode(parentNode, callerBCI, methodID, parseDictionary, false, true, methodAttrs, callAttrs,
						tagAttrs);
				break;

			default:
				handleOther(child);
				break;
			}
		}
	}

	private CompileNode createChildNode(CompileNode parentNode, String callerBCI, String methodID, IParseDictionary parseDictionary,
			boolean inlined, boolean virtualCall, Map<String, String> methodAttrs, Map<String, String> callAttrs,
			Map<String, String> tagAttrs)
	{
		CompileNode childNode = new CompileNode(methodID);
		parentNode.addChild(childNode);

		String reason = tagAttrs.get(ATTR_REASON);
		childNode.setReason(reason);
		childNode.setCallerBCI(Integer.parseInt(callerBCI));

		String tooltip = TooltipUtil.buildInlineAnnotationText(inlined, reason, callAttrs, methodAttrs, parseDictionary);

		childNode.setInlined(inlined);
		childNode.setVirtualCall(virtualCall);
		childNode.setTooltipText(tooltip);

		return childNode;
	}

	@Override public void visitTag(Compilation compilation, Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
		String methodID = parseTag.getAttributes().get(ATTR_METHOD);

		// only initialise on first parse tag.
		// there may be multiple if late_inline
		// is detected
		if (root == null)
		{
			root = CompileNode.createRootNode(compilation, methodID, parseDictionary, model);
		}

		processParseTag(parseTag, root, parseDictionary);
	}
}
