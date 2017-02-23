/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.report.inlining;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class InliningWalker extends AbstractReportBuilder
{
	private BytecodeAnnotationBuilder bcAnnotationBuilder;

	private IMetaMember member;

	public InliningWalker(IReadOnlyJITDataModel model, IMetaMember member)
	{
		super(model);

		this.member = member;

		bcAnnotationBuilder = new BytecodeAnnotationBuilder(false);
	}

	@Override
	protected void findNonMemberReports()
	{
	}

	@Override
	public void visit(IMetaMember metaMember)
	{
		if (metaMember != null && metaMember.isCompiled())
		{
			for (Compilation compilation : metaMember.getCompilations())
			{
				try
				{
					BytecodeAnnotations annotations = bcAnnotationBuilder.buildBytecodeAnnotations(metaMember,
							compilation.getIndex(), model);

					Set<IMetaMember> membersWithAnnotations = annotations.getMembers();

					for (IMetaMember currentMember : membersWithAnnotations)
					{
						BytecodeAnnotationList annotationsForMember = annotations.getAnnotationList(currentMember);

						for (Map.Entry<Integer, List<LineAnnotation>> entry : annotationsForMember.getEntries())
						{
							List<LineAnnotation> lineAnnotations = entry.getValue();

							int bci = entry.getKey();

							for (LineAnnotation la : lineAnnotations)
							{
								if (filterLineAnnotation(la, member))
								{
									ReportType reportType = (la.getType() == BCAnnotationType.INLINE_SUCCESS) ? ReportType.INLINE_SUCCESS : ReportType.INLINE_FAILURE;

									Report report = new Report(currentMember, compilation.getIndex(), bci, la.getAnnotation(),
											reportType, 0, la.getMetaData());

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
	}

	private boolean filterLineAnnotation(LineAnnotation la, IMetaMember child)
	{
		boolean result = false;

		if (la.getType() == BCAnnotationType.INLINE_FAIL || la.getType() == BCAnnotationType.INLINE_SUCCESS)
		{
			Object metaData = la.getMetaData();

			if (metaData != null && metaData instanceof IMetaMember)
			{
				if (child.equals(metaData))
				{
					result = true;
				}
			}
		}

		return result;
	}

	@Override
	public void visitTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
	}
}