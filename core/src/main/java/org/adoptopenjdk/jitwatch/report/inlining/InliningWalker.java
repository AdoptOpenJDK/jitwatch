/*
 * Copyright (c) 2017-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.report.inlining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adoptopenjdk.jitwatch.chain.CompileChainWalker;
import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.bytecode.BCAnnotationType;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationBuilder;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationList;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotations;
import org.adoptopenjdk.jitwatch.model.bytecode.LineAnnotation;
import org.adoptopenjdk.jitwatch.report.AbstractReportBuilder;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.report.ReportType;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class InliningWalker extends AbstractReportBuilder
{
	private CompileChainWalker walker;

	private IMetaMember member;

	public InliningWalker(IReadOnlyJITDataModel model, IMetaMember member)
	{
		super(model);

		this.member = member;

		walker = new CompileChainWalker(model);
	}

	@Override protected void findNonMemberReports()
	{
	}

	private void process(CompileNode parentNode)
	{
		for (CompileNode childNode : parentNode.getChildren())
		{
			if (member.equals(childNode.getMember()))
			{

				ReportType reportType = childNode.isInlined() ? ReportType.INLINE_SUCCESS : ReportType.INLINE_FAILURE;
				//
				String inliningReason = StringUtil.replaceXMLEntities(childNode.getReason());
				//
				//				CompileNode rootCompileNode = parentNode.getRoot();
				//
				//				int compilationIndex = rootCompileNode.getCompilation().getIndex();

				Report report = new Report(childNode, reportType, inliningReason);

				reportList.add(report);
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


/*
		System.out.println(getClass().getName() + " visit " + metaMember);

		if (metaMember != null && metaMember.isCompiled())
		{
			for (Compilation compilation : metaMember.getCompilations())
			{
				try
				{
					BytecodeAnnotations annotations = bcAnnotationBuilder.buildBytecodeAnnotations(metaMember,
							compilation.getIndex(), model);

					Set<IMetaMember> membersWithAnnotations = annotations.getMembers();

					System.out.println("membersWithAnnotations: " + membersWithAnnotations.size());

					for (IMetaMember currentMember : membersWithAnnotations)
					{
						//TODO BROKEN HERE
						// DO NOT USE ANNOTATIONS
						// USE COMPILE NODE GRAPH

						BytecodeAnnotationList annotationsForMember = annotations.getAnnotationList(currentMember);

						System.out.println("member: " + currentMember +" has " + annotationsForMember.getEntries().size());

						for (Map.Entry<Integer, List<LineAnnotation>> entry : annotationsForMember.getEntries())
						{
							List<LineAnnotation> lineAnnotations = entry.getValue();

							int bci = entry.getKey();

							for (LineAnnotation la : lineAnnotations)
							{
								System.out.println(currentMember +" : " + la);

								if (filterLineAnnotation(la, member))
								{
									ReportType reportType = (la.getType() == BCAnnotationType.INLINE_SUCCESS) ? ReportType.INLINE_SUCCESS : ReportType.INLINE_FAILURE;

									Report report = new Report(currentMember, compilation.getIndex(), bci, la.getAnnotation(),
											reportType, 0, la.getMetaData());

									System.out.println(currentMember +" added " + report);


									reportList.add(report);
								}
							}
						}
					}
				}
				catch (AnnotationException e)
				{
					e.printStackTrace();
				}
			}
		}
		*/
	}

	@Override public void visitTag(Compilation compilation, Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
	}
}