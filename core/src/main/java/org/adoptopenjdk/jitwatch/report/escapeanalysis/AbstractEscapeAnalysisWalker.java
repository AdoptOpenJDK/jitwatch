/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.report.escapeanalysis;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CAST_UP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DEPENDENCY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DIRECT_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OBSERVE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PREDICTED_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_UNCOMMON_TRAP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_VIRTUAL_CALL;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ASSERT_NULL;
import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationBuilder;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationList;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotations;
import org.adoptopenjdk.jitwatch.model.bytecode.LineAnnotation;
import org.adoptopenjdk.jitwatch.report.AbstractReportBuilder;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.report.ReportType;

public abstract class AbstractEscapeAnalysisWalker extends AbstractReportBuilder
{
	private BytecodeAnnotationBuilder bcAnnotationBuilder;

	public AbstractEscapeAnalysisWalker(IReadOnlyJITDataModel model)
	{
		super(model);

		ignoreTags.add(TAG_KLASS);
		ignoreTags.add(TAG_TYPE);
		ignoreTags.add(TAG_DEPENDENCY);
		ignoreTags.add(TAG_PARSE_DONE);
		ignoreTags.add(TAG_DIRECT_CALL);
		ignoreTags.add(TAG_PHASE_DONE);
		ignoreTags.add(TAG_INLINE_SUCCESS);
		ignoreTags.add(TAG_UNCOMMON_TRAP);
		ignoreTags.add(TAG_INTRINSIC);
		ignoreTags.add(TAG_PREDICTED_CALL);
		ignoreTags.add(TAG_VIRTUAL_CALL);
		ignoreTags.add(TAG_CAST_UP);
		ignoreTags.add(TAG_OBSERVE);
		ignoreTags.add(TAG_ASSERT_NULL);

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

							boolean inlineAtBCI = hasInlineSuccessAnnotation(lineAnnotations);

							for (LineAnnotation la : lineAnnotations)
							{
								if (filterLineAnnotation(la))
								{
									ReportType type = inlineAtBCI ? ReportType.ELIMINATED_ALLOCATION_INLINE
											: ReportType.ELIMINATED_ALLOCATION_DIRECT;

									Report report = new Report(currentMember, compilation.getIndex(), bci, la.getAnnotation(), type,
											0, la.getMetaData());

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
	
	protected abstract boolean filterLineAnnotation(LineAnnotation la);

	private boolean hasInlineSuccessAnnotation(List<LineAnnotation> annotations)
	{
		boolean result = false;

		for (LineAnnotation la : annotations)
		{
			switch (la.getType())
			{
			case INLINE_SUCCESS:
				result = true;
				break;
			default:
				break;
			}
		}

		return result;
	}

	@Override
	public void visitTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException
	{
	}
}