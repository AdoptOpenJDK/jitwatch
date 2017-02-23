/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SEMICOLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.bytecode.BCAnnotationType;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationBuilder;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationList;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotations;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.LineAnnotation;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.report.ReportType;
import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener.LineType;
import org.adoptopenjdk.jitwatch.ui.triview.TriViewNavigationStack;
import org.adoptopenjdk.jitwatch.ui.triview.Viewer;
import org.adoptopenjdk.jitwatch.util.JVMSUtil;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class ViewerBytecode extends Viewer
{
	private List<BytecodeInstruction> instructions = new ArrayList<>();

	private boolean offsetMismatchDetected = false;
	private IReadOnlyJITDataModel model;
	private TriViewNavigationStack navigationStack;
	private Report lastSuggestion = null;

	public ViewerBytecode(IStageAccessProxy stageAccessProxy, TriViewNavigationStack navigationStack, IReadOnlyJITDataModel model,
			ILineListener lineListener, LineType lineType)
	{
		super(stageAccessProxy, lineListener, lineType, true);
		this.model = model;
		this.navigationStack = navigationStack;
	}

	public void highlightBytecodeForSuggestion(Report report)
	{
		lastSuggestion = report;

		int bytecodeOffset = report.getBytecodeOffset();

		int index = getLineIndexForBytecodeOffset(bytecodeOffset);

		logger.info("highlightBytecodeForSuggestion bci {} index {}", bytecodeOffset, index);

		BytecodeLabel labelAtIndex = (BytecodeLabel) getLabelAtIndex(index);

		if (labelAtIndex != null)
		{
			StringBuilder ttBuilder = new StringBuilder();

			Tooltip tooltip = labelAtIndex.getTooltip();

			if (tooltip != null)
			{
				Tooltip.uninstall(labelAtIndex, tooltip);

				ttBuilder.append(tooltip.getText()).append(S_NEWLINE).append(S_NEWLINE);
			}

			if (report.getType() != ReportType.ELIMINATED_ALLOCATION_DIRECT
					&& report.getType() != ReportType.ELIMINATED_ALLOCATION_INLINE)
			{
				ttBuilder.append("Suggestion:\n");

				String text = report.getText();

				if (report.getType() == ReportType.BRANCH)
				{
					text = StringUtil.wordWrap(text, 50);
				}

				ttBuilder.append(text);
			}

			String toolTipString = ttBuilder.toString();

			toolTipString = StringUtil.replaceXMLEntities(toolTipString);

			tooltip = new Tooltip(toolTipString);

			labelAtIndex.setTooltip(tooltip);
		}

		highlightLine(index);
	}

	public void highlightBytecodeOffset(int bci)
	{
		int index = getLineIndexForBytecodeOffset(bci);
		highlightLine(index);
	}

	public void setContent(final IMetaMember member)
	{
		offsetMismatchDetected = false;
		instructions.clear();

		MemberBytecode memberBytecode = member.getMemberBytecode();

		if (memberBytecode != null)
		{
			instructions.addAll(memberBytecode.getInstructions());
		}

		BytecodeAnnotations bcAnnotations = null;

		lineAnnotations.clear();
		lastScrollIndex = -1;

		List<Label> labels = new ArrayList<>();

		if (!instructions.isEmpty())
		{
			try
			{
				Compilation compilation = member.getSelectedCompilation();

				if (compilation != null)
				{
					int compilationIndex = compilation.getIndex();

					bcAnnotations = new BytecodeAnnotationBuilder(true).buildBytecodeAnnotations(member, compilationIndex, model);
				}
			}
			catch (AnnotationException annoEx)
			{
				logger.error("class bytecode mismatch: {}", annoEx.getMessage());
				logger.error("Member was {}", member);
				offsetMismatchDetected = true;
			}

			int maxOffset = instructions.get(instructions.size() - 1).getOffset();

			int lineIndex = 0;

			for (final BytecodeInstruction instruction : instructions)
			{
				int labelLines = instruction.getLabelLines();

				if (labelLines == 0)
				{
					BytecodeLabel lblLine = createLabel(instruction, maxOffset, 0, bcAnnotations, member, lineIndex++);
					labels.add(lblLine);
				}
				else
				{
					for (int i = 0; i < labelLines; i++)
					{
						BytecodeLabel lblLine = createLabel(instruction, maxOffset, i, bcAnnotations, member, lineIndex++);
						labels.add(lblLine);
					}
				}
			}
		}

		setContent(labels);

		checkIfExistingSuggestionForMember(member);
	}

	private void checkIfExistingSuggestionForMember(IMetaMember member)
	{
		if (lastSuggestion != null && lastSuggestion.getCaller() != null && lastSuggestion.getCaller().equals(member))
		{
			Compilation compilation = member.getSelectedCompilation();

			if (compilation != null && compilation.getIndex() == lastSuggestion.getCompilationIndex())
			{
				highlightBytecodeForSuggestion(lastSuggestion);
			}
		}
	}

	private BytecodeLabel createLabel(final BytecodeInstruction instruction, int maxOffset, int line,
			BytecodeAnnotations bcAnnotations, final IMetaMember member, final int lineIndex)
	{
		BytecodeLabel lblLine = new BytecodeLabel(instruction, maxOffset, line);

		int offset = instruction.getOffset();

		StringBuilder instructionToolTipBuilder = new StringBuilder();

		String unhighlightedStyle = STYLE_UNHIGHLIGHTED;

		boolean hasEliminationAnnotation = false;

		if (bcAnnotations != null)
		{
			BytecodeAnnotationList list = bcAnnotations.getAnnotationList(member);

			if (list != null)
			{
				List<LineAnnotation> annotationList = list.getAnnotationsForBCI(offset);

				if (annotationList != null && !annotationList.isEmpty())
				{
					BCAnnotationType lastAnnotationType = annotationList.get(0).getType();

					Color colour = UserInterfaceUtil.getColourForBytecodeAnnotation(lastAnnotationType);

					unhighlightedStyle = STYLE_UNHIGHLIGHTED + "-fx-text-fill:" + toRGBCode(colour) + C_SEMICOLON;

					instructionToolTipBuilder = new StringBuilder();

					for (LineAnnotation annotation : annotationList)
					{
						if (annotation.getType() != lastAnnotationType || lastAnnotationType != BCAnnotationType.UNCOMMON_TRAP)
						{
							instructionToolTipBuilder.append(S_NEWLINE);
						}

						if (annotation.getType() == BCAnnotationType.ELIMINATED_ALLOCATION
								|| annotation.getType() == BCAnnotationType.LOCK_ELISION)
						{
							hasEliminationAnnotation = true;
						}

						lastAnnotationType = annotation.getType();

						instructionToolTipBuilder.append(annotation.getAnnotation()).append(S_NEWLINE);
					}
				}
			}
		}

		lblLine.setUnhighlightedStyle(unhighlightedStyle);

		if (hasEliminationAnnotation)
		{
			lblLine.getStyleClass().add("eliminated-allocation");
		}

		if (instruction.getOpcode().isInvoke())
		{
			instructionToolTipBuilder.append(S_NEWLINE);
			instructionToolTipBuilder.append("Ctrl-click to inspect this method\nBackspace to return");
		}

		if (instructionToolTipBuilder.length() > 0)
		{
			String toolTipString = StringUtil.replaceXMLEntities(instructionToolTipBuilder.toString().trim());

			Tooltip toolTip = new Tooltip(toolTipString);

			toolTip.setStyle("-fx-strikethrough: false;");
			toolTip.getStyleClass().clear();
			toolTip.getStyleClass().add("tooltip");

			lblLine.setTooltip(toolTip);
		}

		lblLine.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
				{
					if (mouseEvent.getClickCount() == 2)
					{
						Opcode opcode = instruction.getOpcode();

						browseMnemonic(opcode);
					}
					else if (mouseEvent.getClickCount() == 1)
					{
						handleNavigate(member, instruction, lineIndex);
					}
				}
			}
		});

		return lblLine;
	}

	private void handleNavigate(IMetaMember currentMember, BytecodeInstruction instruction, int lineIndex)
	{
		if (navigationStack.isCtrlPressed())
		{
			if (instruction != null && instruction.getOpcode().isInvoke())
			{
				try
				{
					IMetaMember member = ParseUtil.getMemberFromBytecodeComment(model, currentMember, instruction);

					if (member != null)
					{
						navigationStack.navigateTo(member);
					}
				}
				catch (Exception ex)
				{
					logger.error("Could not calculate member for instruction: {}", instruction, ex);
				}
			}
		}
		else
		{
			clearAllHighlighting();

			lineListener.lineHighlighted(lineIndex, lineType);
			highlightLine(lineIndex);
		}
	}

	public boolean isOffsetMismatchDetected()
	{
		return offsetMismatchDetected;
	}

	public int getLineIndexForBytecodeOffset(int bci)
	{
		int result = -1;

		int pos = 0;

		for (BytecodeInstruction instruction : instructions)
		{
			if (instruction.getOffset() == bci)
			{
				result = pos;
				break;
			}

			pos += instruction.getLabelLines();
		}

		return result;
	}

	private String toRGBCode(Color color)
	{
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
	}

	private void browseMnemonic(final Opcode opcode)
	{
		if (JVMSUtil.hasLocalJVMS())
		{
			if (!JVMSUtil.isJVMSLoaded())
			{
				JVMSUtil.loadJVMS();
			}

			String html = JVMSUtil.getBytecodeDescriptions(opcode);

			String cssURI = JVMSUtil.getJVMSCSSURL();

			stageAccessProxy.openBrowser("JMVS Browser - " + opcode.getMnemonic(), html, cssURI);
		}
		else
		{
			stageAccessProxy.openBrowser("Fetching JVM Spec", "Downloading a local copy of the JVM Specification", null);

			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					boolean success = JVMSUtil.fetchJVMS();

					if (success)
					{
						downloadJVMSpecAndShowOpcode(opcode);
					}
					else
					{
						showDownloadFailure();
					}
				}
			}).start();
		}
	}

	private void downloadJVMSpecAndShowOpcode(final Opcode opcode)
	{
		JVMSUtil.loadJVMS();

		final String html = JVMSUtil.getBytecodeDescriptions(opcode);

		final String cssURI = JVMSUtil.getJVMSCSSURL();

		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				stageAccessProxy.openBrowser("JMVS Browser - " + opcode.getMnemonic(), html, cssURI);
			}
		});
	}

	private void showDownloadFailure()
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				stageAccessProxy.openBrowser("Downloading Failed", "Unable to download a local copy of the JVM Specification",
						null);
			}
		});
	}
}