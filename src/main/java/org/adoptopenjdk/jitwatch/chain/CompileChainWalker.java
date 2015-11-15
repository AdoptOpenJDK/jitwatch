/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.chain;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_REASON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_PARSE_HIR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DEPENDENCY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DIRECT_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PREDICTED_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BRANCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_UNCOMMON_TRAP;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.journal.AbstractJournalVisitable;
import org.adoptopenjdk.jitwatch.journal.JournalUtil;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.util.TooltipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompileChainWalker extends AbstractJournalVisitable
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
	}

	public CompileNode buildCallTree(Journal journal)
	{
		this.root = null;

		try
		{
			JournalUtil.visitParseTagsOfLastTask(journal, this);
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
		CompileNode lastNode = null;

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
				handleInline(parentNode, methodID, parseDictionary, false, methodAttrs, callAttrs, tagAttrs);
				methodID = null;
				lastNode = null;
				break;
			}

			case TAG_INLINE_SUCCESS:
			{
				lastNode = handleInline(parentNode, methodID, parseDictionary, true, methodAttrs, callAttrs, tagAttrs);
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
				else if (child.getNamedChildren(TAG_PARSE).size() > 0)
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

			default:
				handleOther(child);
				break;
			}
		}
	}

	private CompileNode handleInline(CompileNode parentNode, String methodID, IParseDictionary parseDictionary, boolean inlined,
			Map<String, String> methodAttrs, Map<String, String> callAttrs, Map<String, String> tagAttrs)
	{
		CompileNode childNode = new CompileNode(methodID);
		parentNode.addChild(childNode);

		String reason = tagAttrs.get(ATTR_REASON);
		String tooltip = TooltipUtil.buildInlineAnnotationText(inlined, reason, callAttrs, methodAttrs, parseDictionary);
		
		childNode.setInlined(inlined);
		childNode.setCompiled(methodAttrs.containsKey(ATTR_COMPILE_ID));
		childNode.setTooltipText(tooltip);
		
		return childNode;
	}

	@Override
	public void visitTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
		String methodID = parseTag.getAttribute(ATTR_METHOD);

		// only initialise on first parse tag.
		// there may be multiple if late_inline
		// is detected
		if (root == null)
		{
			root = CompileNode.createRootNode(methodID, parseDictionary, model);
		}

		processParseTag(parseTag, root, parseDictionary);
	}
}