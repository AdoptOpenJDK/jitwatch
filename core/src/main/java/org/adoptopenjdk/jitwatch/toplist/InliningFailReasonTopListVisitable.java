/*
 * Copyright (c) 2013-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.chain.CompileChainWalker;
import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.util.StringUtil;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class InliningFailReasonTopListVisitable extends AbstractTopListVisitable
{
	private Map<String, List<CompileNode>> reasonToCompileNodesMap;

	private CompileChainWalker walker;

	public InliningFailReasonTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);

		reasonToCompileNodesMap = new HashMap<>();

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

		walker = new CompileChainWalker(model);
	}

	@Override public void visit(IMetaMember metaMember)
	{
		if (metaMember != null && metaMember.isCompiled())
		{
			for (Compilation compilation : metaMember.getCompilations())
			{
				CompileNode rootCompileNodeForMember = walker.buildCallTree(compilation);

				System.out.println(metaMember.getAbbreviatedFullyQualifiedMemberName());
				System.out.println(rootCompileNodeForMember);

				if (rootCompileNodeForMember != null)
				{
					process(rootCompileNodeForMember);
				}
			}
		}
	}

	private void process(CompileNode node)
	{
		for (CompileNode child : node.getChildren())
		{
			if (!child.isInlined())
			{
				String inliningFailureReason = StringUtil.replaceXMLEntities(child.getReason());

				List<CompileNode> nodesForReason = reasonToCompileNodesMap.get(inliningFailureReason);

				if (nodesForReason == null)
				{
					nodesForReason = new ArrayList<>();
					reasonToCompileNodesMap.put(inliningFailureReason, nodesForReason);
				}

				nodesForReason.add(child);
			}

			process(child);
		}
	}

	@Override public void reset()
	{
		reasonToCompileNodesMap.clear();
		walker.clear();
	}

	@Override public void postProcess()
	{
		for (Map.Entry<String, List<CompileNode>> entry : reasonToCompileNodesMap.entrySet())
		{
			String reason = entry.getKey();

			List<CompileNode> compilations = entry.getValue();

			CompilationListScore score = new CompilationListScore(reason, compilations);

			topList.add(score);
		}
	}

	@Override public void visitTag(Compilation compilation, Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
	}
}