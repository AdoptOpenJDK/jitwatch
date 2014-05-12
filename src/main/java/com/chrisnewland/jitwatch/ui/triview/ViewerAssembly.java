package com.chrisnewland.jitwatch.ui.triview;

import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import com.chrisnewland.jitwatch.model.assembly.AssemblyBlock;
import com.chrisnewland.jitwatch.model.assembly.AssemblyInstruction;
import com.chrisnewland.jitwatch.model.assembly.AssemblyMethod;
import com.chrisnewland.jitwatch.ui.IStageAccessProxy;
// https://github.com/yasm/yasm/wiki/GasSyntax
// http://x86asm.net/articles/what-i-dislike-about-gas/
// http://vaskoz.wordpress.com/2013/07/14/hotspot-disassembler/
public class ViewerAssembly extends Viewer
{
	public ViewerAssembly(IStageAccessProxy stageAccessProxy)
	{
		super(stageAccessProxy);
	}

	public void setAssemblyMethod(AssemblyMethod asmMethod)
	{
		lastScrollIndex = -1;

		List<Label> labels = new ArrayList<>();

		labels.add(createLabel(asmMethod.getHeader()));

		for (AssemblyBlock block : asmMethod.getBlocks())
		{
			String title = block.getTitle();

			if (title != null)
			{
				labels.add(createLabel(title));
			}

			for (final AssemblyInstruction instr : block.getInstructions())
			{
				Label lblLine = createLabel(instr.toString());

				lblLine.setOnMouseClicked(new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent mouseEvent)
					{
						if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
						{
							if (mouseEvent.getClickCount() == 2)
							{
								System.out.println(instr.getMnemonic());
							}
						}
					}
				});
				
				labels.add(lblLine);

			}
		}

		setContent(labels);
	}
	
	private Label createLabel(String text)
	{
		Label lbl = new Label(text);

		lbl.setStyle(STYLE_UNHIGHLIGHTED);
		
		return lbl;
	}
}
