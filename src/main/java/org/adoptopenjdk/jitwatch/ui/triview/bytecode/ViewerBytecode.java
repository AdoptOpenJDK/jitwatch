/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SEMICOLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LineAnnotation;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationBuilder;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.adoptopenjdk.jitwatch.suggestion.Suggestion;
import org.adoptopenjdk.jitwatch.suggestion.Suggestion.SuggestionType;
import org.adoptopenjdk.jitwatch.ui.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener.LineType;
import org.adoptopenjdk.jitwatch.ui.triview.TriViewNavigationStack;
import org.adoptopenjdk.jitwatch.ui.triview.Viewer;
import org.adoptopenjdk.jitwatch.util.JVMSUtil;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class ViewerBytecode extends Viewer
{
	private List<BytecodeInstruction> instructions = new ArrayList<>();

	private boolean offsetMismatchDetected = false;
	private IReadOnlyJITDataModel model;
	private TriViewNavigationStack navigationStack;
	private Suggestion lastSuggestion = null;

	public ViewerBytecode(IStageAccessProxy stageAccessProxy, TriViewNavigationStack navigationStack, IReadOnlyJITDataModel model,
			ILineListener lineListener, LineType lineType)
	{
		super(stageAccessProxy, lineListener, lineType, true);
		this.model = model;
		this.navigationStack = navigationStack;
	}

	public void highlightBytecodeForSuggestion(Suggestion suggestion)
	{
		lastSuggestion = suggestion;

		int bytecodeOffset = suggestion.getBytecodeOffset();

		int index = getLineIndexForBytecodeOffset(bytecodeOffset);

		BytecodeLabel labelAtIndex = (BytecodeLabel) getLabelAtIndex(index);

		if (labelAtIndex != null)
		{
			StringBuilder ttBuilder = new StringBuilder();

			Tooltip tooltip = labelAtIndex.getTooltip();

			if (tooltip != null)
			{
				ttBuilder.append(tooltip.getText()).append(S_NEWLINE).append(S_NEWLINE);
				Tooltip.uninstall(labelAtIndex, tooltip);
			}

			ttBuilder.append("Suggestion:\n");

			String text = suggestion.getText();

			if (suggestion.getType() == SuggestionType.BRANCH)
			{
				text = StringUtil.wordWrap(text, 50);
			}

			ttBuilder.append(text);

			tooltip = new Tooltip(ttBuilder.toString());
			labelAtIndex.setTooltip(tooltip);
		}

		highlightLine(index);
	}

	public void highlightBytecodeOffset(int bci)
	{
		int index = getLineIndexForBytecodeOffset(bci);
		highlightLine(index);
	}

	public void setContent(final IMetaMember member, final ClassBC metaClassBytecode, final List<String> classLocations)
	{
		offsetMismatchDetected = false;

		if (metaClassBytecode != null)
		{
			MemberBytecode memberBytecode = metaClassBytecode.getMemberBytecode(member);

			if (memberBytecode != null)
			{
				instructions = memberBytecode.getInstructions();
			}
		}

		Map<Integer, LineAnnotation> annotations = new HashMap<Integer, LineAnnotation>();

		lineAnnotations.clear();
		lastScrollIndex = -1;

		List<Label> labels = new ArrayList<>();

		if (instructions != null && instructions.size() > 0)
		{
			try
			{
				annotations = new BytecodeAnnotationBuilder().buildBytecodeAnnotations(member, instructions);
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
					BytecodeLabel lblLine = createLabel(instruction, maxOffset, 0, annotations, member, lineIndex++);
					labels.add(lblLine);
				}
				else
				{
					for (int i = 0; i < labelLines; i++)
					{
						BytecodeLabel lblLine = createLabel(instruction, maxOffset, i, annotations, member, lineIndex++);
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
		if (lastSuggestion != null && lastSuggestion.getCaller().equals(member))
		{
			highlightBytecodeForSuggestion(lastSuggestion);
		}
	}

	private BytecodeLabel createLabel(final BytecodeInstruction instruction, int maxOffset, int line,
			final Map<Integer, LineAnnotation> annotations, final IMetaMember member, final int lineIndex)
	{
		BytecodeLabel lblLine = new BytecodeLabel(instruction, maxOffset, line);

		int offset = instruction.getOffset();

		StringBuilder instructionToolTipBuilder = new StringBuilder();

		String unhighlightedStyle = STYLE_UNHIGHLIGHTED;

		if (annotations != null)
		{
			LineAnnotation annotation = annotations.get(offset);

			if (annotation != null)
			{
				Color colour = annotation.getColour();

				unhighlightedStyle = STYLE_UNHIGHLIGHTED + "-fx-text-fill:" + toRGBCode(colour) + C_SEMICOLON;

				instructionToolTipBuilder = new StringBuilder();
				instructionToolTipBuilder.append(annotation.getAnnotation());
			}
		}

		lblLine.setUnhighlightedStyle(unhighlightedStyle);

		if (instruction.isEliminated())
		{
			lblLine.getStyleClass().add("eliminated-allocation");
		}
		
		if (instruction.isInvoke())
		{
			if (instructionToolTipBuilder.length() > 0)
			{
				instructionToolTipBuilder.append(S_NEWLINE).append(S_NEWLINE);
			}

			instructionToolTipBuilder.append("Ctrl-click to inspect this method\nBackspace to return");
		}

		if (instructionToolTipBuilder.length() > 0)
		{
			Tooltip toolTip = new Tooltip(instructionToolTipBuilder.toString());

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
			if (instruction != null && instruction.isInvoke())
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
				stageAccessProxy
						.openBrowser("Downloading Failed", "Unable to download a local copy of the JVM Specification", null);
			}
		});
	}
}