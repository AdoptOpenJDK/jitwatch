package com.chrisnewland.jitwatch.ui.triview.bytecode;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.LineAnnotation;
import com.chrisnewland.jitwatch.model.bytecode.BytecodeInstruction;
import com.chrisnewland.jitwatch.model.bytecode.MemberBytecode;
import com.chrisnewland.jitwatch.model.bytecode.Opcode;
import com.chrisnewland.jitwatch.ui.IStageAccessProxy;
import com.chrisnewland.jitwatch.ui.triview.ILineListener;
import com.chrisnewland.jitwatch.ui.triview.ILineListener.LineType;
import com.chrisnewland.jitwatch.ui.triview.Viewer;
import com.chrisnewland.jitwatch.util.JVMSUtil;
import com.chrisnewland.jitwatch.util.JournalUtil;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewerBytecode extends Viewer
{
    private static final int MAX_RED_COLOUR = 255;
    private static final int MAX_GREEN_COLOUR = 255;
    private static final int MAX_BLUE_COLOUR = 255;
    private MemberBytecode memberBytecode;

	public ViewerBytecode(IStageAccessProxy stageAccessProxy, ILineListener lineListener, LineType lineType)
	{
		super(stageAccessProxy, lineListener, lineType, true);
	}

	public void setContent(IMetaMember member, List<String> classLocations)
	{
		memberBytecode = member.getBytecodeForMember(classLocations);

		List<BytecodeInstruction> instructions = memberBytecode.getBytecodeInstructions();
		
		Map<Integer, LineAnnotation> annotations = null;

		lineAnnotations.clear();
		lastScrollIndex = -1;

		List<Label> labels = new ArrayList<>();

		if (instructions != null && instructions.size() > 0)
		{
			Journal journal = member.getJournal();

			annotations = JournalUtil.buildBytecodeAnnotations(journal, instructions);
			
			int maxOffset = instructions.get(instructions.size() - 1).getOffset();

			for (final BytecodeInstruction instruction : instructions)
			{
				Label lblLine = new Label(instruction.toString(maxOffset));

				labels.add(lblLine);

				int offset = instruction.getOffset();

				String annotationText = null;

				if (annotations != null)
				{
					LineAnnotation annotation = annotations.get(offset);

					if (annotation != null)
					{
						annotationText = annotation.getAnnotation();
						Color colour = annotation.getColour();

						lblLine.setStyle(STYLE_UNHIGHLIGHTED + "-fx-text-fill:" + toRGBCode(colour) + ";");

						lblLine.setTooltip(new Tooltip(annotationText));
					}
					else
					{
						lblLine.setStyle(STYLE_UNHIGHLIGHTED);
					}
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
	
	public int getLineIndexForBytecodeOffset(int offset)
	{
		int result = -1;
		
		int pos = 0;
		
		for (BytecodeInstruction instruction : memberBytecode.getBytecodeInstructions())
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
		return String.format("#%02X%02X%02X",
                (int) (color.getRed() * MAX_RED_COLOUR),
                (int) (color.getGreen() * MAX_GREEN_COLOUR),
				(int) (color.getBlue() * MAX_BLUE_COLOUR));
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
					else
					{
						Platform.runLater(new Runnable()
						{
							@Override
							public void run()
							{
								stageAccessProxy.openBrowser("Downloading Failed",
										"Unable to download a local copy of the JVM Specification", null);
							}
						});
					}
				}
			}).start();
		}
	}
}
