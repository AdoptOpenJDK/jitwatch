package com.chrisnewland.jitwatch.ui.triview;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import com.chrisnewland.jitwatch.model.assembly.AssemblyBlock;
import com.chrisnewland.jitwatch.model.assembly.AssemblyInstruction;
import com.chrisnewland.jitwatch.model.assembly.AssemblyMethod;
import com.chrisnewland.jitwatch.model.assembly.AssemblyReference;
import com.chrisnewland.jitwatch.ui.IStageAccessProxy;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

// https://github.com/yasm/yasm/wiki/GasSyntax
// http://x86asm.net/articles/what-i-dislike-about-gas/
// http://vaskoz.wordpress.com/2013/07/14/hotspot-disassembler/
// https://sourceware.org/binutils/docs/as/i386_002dRegs.html#i386_002dRegs

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
				final Label lblLine = createLabel(instr.toString());

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

				lblLine.setOnMouseEntered(new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent arg0)
					{
						lblLine.setStyle(STYLE_HIGHLIGHTED);
					}
				});
				
				lblLine.setOnMouseExited(new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent arg0)
					{
						lblLine.setStyle(STYLE_UNHIGHLIGHTED);
					}
				});


				lblLine.setTooltip(new Tooltip(getToolTip(instr)));
				
				labels.add(lblLine);
			}
		}

		setContent(labels);
	}
	
	private String getToolTip(AssemblyInstruction instruction)
	{
		StringBuilder builder = new StringBuilder();
		
		String ref = AssemblyReference.lookupMnemonic(instruction.getMnemonic());
		
		if (ref == null)
		{
			ref = "Unknown instruction";
		}
		
		builder.append(ref).append(S_NEWLINE);
		
		List<String> operands = instruction.getOperands();
		
		int opCount = operands.size();
		
		if (opCount >= 1)
		{
			builder.append("Source: ");
			
			String op1 = operands.get(0);
			
			if (op1.startsWith(S_PERCENT))
			{
				builder.append(decodeRegister(op1, true));
			}
			else if (op1.startsWith(S_DOLLAR))
			{
				builder.append("Constant ");
				builder.append(op1);
			}
			else if (op1.startsWith(S_ASSEMBLY_ADDRESS) || (op1.contains(S_OPEN_PARENTHESES) && op1.contains(S_CLOSE_PARENTHESES)))
			{
				builder.append("Address ");
				builder.append(op1);
			}
			
			if (opCount == 2)
			{
				builder.append("\nDestination: ");
				
				String op2 = operands.get(1);
				
				if (op2.startsWith(S_PERCENT))
				{
					builder.append(decodeRegister(op2, true));
				}
				else if (op1.startsWith(S_DOLLAR))
				{
					builder.append("Constant ");
					builder.append(op2);
				}
				else if (op2.startsWith(S_ASSEMBLY_ADDRESS) || (op2.contains(S_OPEN_PARENTHESES) && op2.contains(S_CLOSE_PARENTHESES)))
				{
					builder.append("Address ");
					builder.append(op2);
				}
			}
		}
				
		return builder.toString();		
	}

	private String decodeRegister(String register, boolean isATT)
	{
		StringBuilder builder = new StringBuilder();
		
		if (isATT)
		{
			// https://sourceware.org/binutils/docs/as/i386_002dRegs.html#i386_002dRegs
			// http://www.x86-64.org/documentation/assembly.html
			if (register.length() >= 3)
			{
				String regName = register.substring(1); // remove %
				
				if (regName.startsWith("e"))
				{
					builder.append("32-bit register ").append(register);
				}
				else if (regName.startsWith("r"))
				{
					builder.append("64-bit register ").append(register);
				}
				else if (regName.startsWith("db"))
				{
					builder.append("debug register ").append(register);
				}
				else if (regName.startsWith("cr"))
				{
					builder.append("processor control register ").append(register);
				}
				else if (regName.startsWith("tr"))
				{
					builder.append("test register ").append(register);
				}
				else if (regName.startsWith("st"))
				{
					builder.append("floating point register stack ").append(register);
				}
				else if (regName.startsWith("mm"))
				{
					builder.append("MMX register ").append(register);
				}
				else if (regName.startsWith("xmm"))
				{
					builder.append("SSE register ").append(register);
				}
				else if (regName.endsWith("s"))
				{
					builder.append("section register ").append(register);
				}
				else
				{
					builder.append("Register ").append(register);
				}
				
				if (regName.endsWith("ax"))
				{
					builder.append(" (accumulator)");
				}
				else if (regName.endsWith("bp"))
				{
					builder.append(" (frame pointer)");
				}
				else if (regName.endsWith("sp"))
				{
					builder.append(" (stack pointer)");
				}				
			}
			else
			{
				builder.append("unknown register: ").append(register);
			}
		}
		
		return builder.toString();
	}
	
	private Label createLabel(String text)
	{
		Label lbl = new Label(text);

		lbl.setStyle(STYLE_UNHIGHLIGHTED);

		return lbl;
	}
}
