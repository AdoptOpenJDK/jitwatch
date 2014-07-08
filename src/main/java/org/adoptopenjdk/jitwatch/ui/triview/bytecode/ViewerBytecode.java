package org.adoptopenjdk.jitwatch.ui.triview.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.model.LineAnnotation;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.adoptopenjdk.jitwatch.ui.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener;
import org.adoptopenjdk.jitwatch.ui.triview.Viewer;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener.LineType;
import org.adoptopenjdk.jitwatch.util.JVMSUtil;
import org.adoptopenjdk.jitwatch.util.JournalUtil;

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

	public ViewerBytecode(IStageAccessProxy stageAccessProxy, ILineListener lineListener, LineType lineType)
	{
		super(stageAccessProxy, lineListener, lineType, true);
	}

	public void setContent(IMetaMember member, ClassBC metaClassBytecode, List<String> classLocations)
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
				logger.error("class bytcode mismatch: {}", annoEx.getMessage());

				offsetMismatchDetected = true;
			}

			int maxOffset = instructions.get(instructions.size() - 1).getOffset();

			for (final BytecodeInstruction instruction : instructions)
			{
				BytecodeLabel lblLine = new BytecodeLabel(instruction, maxOffset);

				labels.add(lblLine);

				int offset = instruction.getOffset();

				String annotationText = null;

				if (annotations != null)
				{
					LineAnnotation annotation = annotations.get(offset);

					String unhighlightedStyle = STYLE_UNHIGHLIGHTED;

					if (annotation != null)
					{
						annotationText = annotation.getAnnotation();
						Color colour = annotation.getColour();

						unhighlightedStyle = STYLE_UNHIGHLIGHTED + "-fx-text-fill:" + toRGBCode(colour) + ";";

						lblLine.setTooltip(new Tooltip(annotationText));
					}

					lblLine.setUnhighlightedStyle(unhighlightedStyle);
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
						}
					}
				});
			}
		}

		setContent(labels);
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