/*
 * Copyright (c) 2017-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.chain;

import org.adoptopenjdk.jitwatch.compilation.AbstractCompilationVisitable;
import org.adoptopenjdk.jitwatch.model.*;
import org.adoptopenjdk.jitwatch.report.AbstractReportBuilder;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.report.ReportType;
import org.adoptopenjdk.jitwatch.treevisitor.ITreeVisitable;
import org.adoptopenjdk.jitwatch.treevisitor.TreeVisitor;
import org.adoptopenjdk.jitwatch.util.StringUtil;

import java.util.*;

public class RootFinder  extends AbstractCompilationVisitable implements ITreeVisitable
{
	private CompileChainWalker walker;

	private IMetaMember member;

	private Set<CompileNode> rootNodes;

	public RootFinder(IReadOnlyJITDataModel model, IMetaMember member)
	{
		this.member = member;

		rootNodes = new HashSet<>();

		walker = new CompileChainWalker(model);

		TreeVisitor.walkTree(model, this);
	}

	public void reset()
	{
		rootNodes.clear();
	}

	public Set<CompileNode> getResult()
	{
		return rootNodes;
	}

	private void process(CompileNode parentNode)
	{
		for (CompileNode childNode : parentNode.getChildren())
		{
			if (member.equals(childNode.getMember()))
			{
				rootNodes.add(parentNode.getRoot());
				return;
			}

			process(childNode);
		}
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

	@Override public void visitTag(Compilation compilation, Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
	}
}