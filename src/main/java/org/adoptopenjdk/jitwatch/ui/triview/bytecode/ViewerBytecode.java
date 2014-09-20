/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.model.LineAnnotation;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.adoptopenjdk.jitwatch.ui.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener;
import org.adoptopenjdk.jitwatch.ui.triview.TriViewNavigationStack;
import org.adoptopenjdk.jitwatch.ui.triview.Viewer;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener.LineType;
import org.adoptopenjdk.jitwatch.util.JVMSUtil;
import org.adoptopenjdk.jitwatch.util.JournalUtil;
import org.adoptopenjdk.jitwatch.util.ParseUtil;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
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

	public ViewerBytecode(IStageAccessProxy stageAccessProxy, TriViewNavigationStack navigationStack, IReadOnlyJITDataModel model, ILineListener lineListener,
			LineType lineType)
	{
		super(stageAccessProxy, lineListener, lineType, true);
		this.model = model;
		this.navigationStack = navigationStack;
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
			Journal journal = member.getJournal();

			try
			{
				annotations = JournalUtil.buildBytecodeAnnotations(journal, instructions);
			}
			catch (AnnotationException annoEx)
			{
				logger.error("class bytecode mismatch: {}", annoEx.getMessage());
				logger.error("Member was {}", member);
				offsetMismatchDetected = true;
			}

			int maxOffset = instructions.get(instructions.size() - 1).getOffset();
			
			for (final BytecodeInstruction instruction : instructions)
			{				
				int labelLines = instruction.getLabelLines();
				
				if (labelLines == 0)
				{
					BytecodeLabel lblLine = createLabel(instruction, maxOffset, 0, annotations, member);
					labels.add(lblLine);
				}
				else
				{
					for (int i = 0; i < labelLines; i++)
					{
						BytecodeLabel lblLine = createLabel(instruction, maxOffset, i, annotations, member);
						labels.add(lblLine);
					}
				}
			}
		}

		setContent(labels);
	}
	
	private BytecodeLabel createLabel(final BytecodeInstruction instruction, int maxOffset, int line, final Map<Integer, LineAnnotation> annotations, final IMetaMember member)
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
			lblLine.setTooltip(new Tooltip(instructionToolTipBuilder.toString()));
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
						handleNavigate(member, instruction);
					}
				}
			}
		});
	
		return lblLine;
	}

	private void handleNavigate(IMetaMember currentMember, BytecodeInstruction instruction)
	{
		if (navigationStack.isCtrlPressed())
		{
			if (instruction != null && instruction.isInvoke())
			{
				String comment = instruction.getCommentWithMethodPrefixStripped();

				if (comment != null)
				{
					if (commentMethodHasNoClassPrefix(comment))
					{
						comment = prependCurrentMember(comment, currentMember);
					}

					try
					{
						IMetaMember member = ParseUtil.getMemberFromComment(model, comment);

						if (member != null)
						{
							navigationStack.navigateTo(member);
						}
					}
					catch (Exception ex)
					{
						logger.error("Could not calculate member for comment: {}", comment, ex);
					}
				}
			}
		}
	}
	
	private boolean commentMethodHasNoClassPrefix(String comment)
	{
		return (comment.indexOf(C_DOT) == -1);
	}

	private String prependCurrentMember(String comment, IMetaMember member)
	{
		String currentClass = member.getMetaClass().getFullyQualifiedName();
		
		currentClass = currentClass.replace(C_DOT, C_SLASH);

		return currentClass + C_DOT + comment;
	}
	
	public boolean isOffsetMismatchDetected()
	{
		return offsetMismatchDetected;
	}

	public int getLineIndexForBytecodeOffset(int offset)
	{
		int result = -1;

		int pos = 0;

		for (BytecodeInstruction instruction : instructions)
		{
			if (instruction.getOffset() == offset)
			{
				result = pos;
				break;
			}

			pos++;
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