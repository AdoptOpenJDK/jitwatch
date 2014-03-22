package com.chrisnewland.jitwatch.ui.triview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import com.chrisnewland.jitwatch.model.LineAnnotation;
import com.chrisnewland.jitwatch.model.bytecode.Instruction;
import com.chrisnewland.jitwatch.model.bytecode.Opcode;
import com.chrisnewland.jitwatch.ui.IStageAccessProxy;
import com.chrisnewland.jitwatch.util.BytecodeUtil;

public class ViewerBytecode extends Viewer
{
	private List<Instruction> instructions;

	public ViewerBytecode(IStageAccessProxy stageAccessProxy)
	{
		super(stageAccessProxy);
	}

	public void setContent(String bytecode, Map<Integer, LineAnnotation> annotations)
	{
		instructions = BytecodeUtil.parseInstructions(bytecode);

		lineAnnotations.clear();
		lastScrollIndex = -1;

		if (bytecode == null)
		{
			bytecode = "Empty";
		}

		originalSource = bytecode;

		List<Label> labels = new ArrayList<>();

		if (instructions.size() > 0)
		{
			int maxOffset = instructions.get(instructions.size() - 1).getOffset();

			for (final Instruction instruction : instructions)
			{
				Label lblLine = new Label(instruction.toString(maxOffset));

				lblLine.setStyle(STYLE_UNHIGHLIGHTED);

				labels.add(lblLine);

				int offset = instruction.getOffset();

				String annotationText = null;

				LineAnnotation annotation = annotations.get(offset);

				if (annotation != null)
				{
					annotationText = annotation.getAnnotation();
					Color colour = annotation.getColour();

					lblLine.setTextFill(colour);
					lblLine.setTooltip(new Tooltip(annotationText));
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
								String mnemonic = instruction.getMnemonic();

								browseMnemonic(mnemonic);
							}
						}
					}
				});
			}
		}

		setContent(labels);
	}

	private void browseMnemonic(final String mnemonic)
	{
		final Opcode opcode = Opcode.getOpcode(mnemonic);

		if (BytecodeUtil.hasLocalJVMS())
		{
			if (!BytecodeUtil.isJVMSLoaded())
			{
				BytecodeUtil.loadJVMS();
			}

			String html = BytecodeUtil.getBytecodeDescriptions(opcode);

			String cssURI = BytecodeUtil.getJVMSCSSURL();

			stageAccessProxy.openBrowser("JMVS Browser - " + mnemonic, html, cssURI);
		}
		else
		{
			stageAccessProxy.openBrowser("Fetching JVM Spec", "Downloading a local copy of the JVM Specification", null);

			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					boolean success = BytecodeUtil.fetchJVMS();

					if (success)
					{
						BytecodeUtil.loadJVMS();

						final String html = BytecodeUtil.getBytecodeDescriptions(opcode);

						final String cssURI = BytecodeUtil.getJVMSCSSURL();

						Platform.runLater(new Runnable()
						{
							@Override
							public void run()
							{
								stageAccessProxy.openBrowser("JMVS Browser - " + mnemonic, html, cssURI);
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
