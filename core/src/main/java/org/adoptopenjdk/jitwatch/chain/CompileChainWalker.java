/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.chain;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_REASON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_PARSE_HIR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BRANCH;
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
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PREDICTED_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_UNCOMMON_TRAP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OBSERVE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_HOT_THROW;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_VIRTUAL_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CAST_UP;

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

public class CompileChainWalker extends AbstractCompilationVisitable
{
	private static final Logger logger = LoggerFactory.getLogger(CompileChainWalker.class);

	private IReadOnlyJITDataModel model;

	private CompileNode root = null;
	
	private Compilation compilation;

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
		ignoreTags.add(TAG_HOT_THROW);
		ignoreTags.add(TAG_CAST_UP);
		ignoreTags.add(TAG_HOT_THROW);
	}

	public CompileNode buildCallTree(Compilation compilation)
	{
		this.root = null;
		
		this.compilation = compilation;

		try
		{
			CompilationUtil.visitParseTagsOfCompilation(compilation, this);
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
				createChildNode(parentNode, methodID, parseDictionary, false, false, methodAttrs, callAttrs, tagAttrs);
				methodID = null;
				lastNode = null;
				break;
			}

			case TAG_INLINE_SUCCESS:
			{
				lastNode = createChildNode(parentNode, methodID, parseDictionary, true, false, methodAttrs, callAttrs, tagAttrs);
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
			
			case TAG_VIRTUAL_CALL:
				lastNode = createChildNode(parentNode, methodID, parseDictionary, false, true, methodAttrs, callAttrs, tagAttrs);
				break;

			default:
				handleOther(child);
				break;
			}
		}
	}

	private CompileNode createChildNode(CompileNode parentNode, String methodID, IParseDictionary parseDictionary, boolean inlined, boolean virtualCall,
			Map<String, String> methodAttrs, Map<String, String> callAttrs, Map<String, String> tagAttrs)
	{
		CompileNode childNode = new CompileNode(methodID);
		parentNode.addChild(childNode);

		String reason = tagAttrs.get(ATTR_REASON);
		String tooltip = TooltipUtil.buildInlineAnnotationText(inlined, reason, callAttrs, methodAttrs, parseDictionary);
		
		childNode.setInlined(inlined);
		childNode.setVirtualCall(virtualCall);
		childNode.setTooltipText(tooltip);
		
		return childNode;
	}

	@Override
	public void visitTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
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
