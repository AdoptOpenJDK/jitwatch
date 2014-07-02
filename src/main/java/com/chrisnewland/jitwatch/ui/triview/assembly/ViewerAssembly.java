package com.chrisnewland.jitwatch.ui.triview.assembly;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import com.chrisnewland.jitwatch.model.assembly.AssemblyBlock;
import com.chrisnewland.jitwatch.model.assembly.AssemblyInstruction;
import com.chrisnewland.jitwatch.model.assembly.AssemblyMethod;
import com.chrisnewland.jitwatch.model.assembly.AssemblyReference;
import com.chrisnewland.jitwatch.ui.IStageAccessProxy;
import com.chrisnewland.jitwatch.ui.triview.ILineListener;
import com.chrisnewland.jitwatch.ui.triview.Viewer;
import com.chrisnewland.jitwatch.ui.triview.ILineListener.LineType;
import com.chrisnewland.jitwatch.util.StringUtil;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

// https://github.com/yasm/yasm/wiki/GasSyntax
// http://x86asm.net/articles/what-i-dislike-about-gas/
// http://vaskoz.wordpress.com/2013/07/14/hotspot-disassembler/
// https://sourceware.org/binutils/docs/as/i386_002dRegs.html#i386_002dRegs

public class ViewerAssembly extends Viewer
{
	public ViewerAssembly(IStageAccessProxy stageAccessProxy, ILineListener lineListener, LineType lineType)
	{
		super(stageAccessProxy, lineListener, lineType, true);
	}

	public void setAssemblyMethod(AssemblyMethod asmMethod)
	{
		lastScrollIndex = -1;

		List<Label> labels = new ArrayList<>();

		String header = asmMethod.getHeader();
		
		if (header != null)
		{
			String[] headerLines = header.split(S_NEWLINE);
			
			for (String headerLine : headerLines)
			{
				labels.add(createLabel(headerLine));
			}
		}		

		for (AssemblyBlock block : asmMethod.getBlocks())
		{
			String title = block.getTitle();

			if (title != null)
			{
				labels.add(createLabel(title));
			}

			for (final AssemblyInstruction instr : block.getInstructions())
			{
				List<String> commentLines = instr.getCommentLines();

				if (commentLines.size() == 0)
				{
					Label lblLine = createLabel(instr, 0);
					lblLine.setTooltip(new Tooltip(getToolTip(instr)));
					labels.add(lblLine);
				}
				else
				{
					for (int i = 0; i < commentLines.size(); i++)
					{
						Label lblLine = createLabel(instr, i);
						lblLine.setTooltip(new Tooltip(getToolTip(instr)));
						labels.add(lblLine);
					}
				}
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
				else if (op2.startsWith(S_ASSEMBLY_ADDRESS)
						|| (op2.contains(S_OPEN_PARENTHESES) && op2.contains(S_CLOSE_PARENTHESES)))
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
					builder.append("32-bit register ").append(regName);
				}
				else if (regName.startsWith("r"))
				{
					builder.append("64-bit register ").append(regName);
				}
				else if (regName.startsWith("db"))
				{
					builder.append("debug register ").append(regName);
				}
				else if (regName.startsWith("cr"))
				{
					builder.append("processor control register ").append(regName);
				}
				else if (regName.startsWith("tr"))
				{
					builder.append("test register ").append(regName);
				}
				else if (regName.startsWith("st"))
				{
					builder.append("floating point register stack ").append(regName);
				}
				else if (regName.startsWith("mm"))
				{
					builder.append("MMX register ").append(regName);
				}
				else if (regName.startsWith("xmm"))
				{
					builder.append("SSE register ").append(regName);
				}
				else if (regName.endsWith("s"))
				{
					builder.append("section register ").append(regName);
				}
				else
				{
					builder.append("Register ").append(regName);
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

	private AssemblyLabel createLabel(AssemblyInstruction instruction, int line)
	{
		AssemblyLabel lbl = new AssemblyLabel(instruction, line);

		lbl.setStyle(STYLE_UNHIGHLIGHTED);

		return lbl;
	}

	public String getClassNameFromLabel(Label label)
	{
		String result = null;

		if (label != null)
		{
			String line = label.getText();

			result = StringUtil.getSubstringBetween(line, "; - ", "::");
		}

		return result;
	}

	public String getSourceLineFromLabel(Label label)
	{
		String result = null;

		if (label != null)
		{
			String line = label.getText();

			result = StringUtil.getSubstringBetween(line, "(line ", S_CLOSE_PARENTHESES);
		}

		return result;
	}

	public String getBytecodeOffsetFromLabel(Label label)
	{
		String result = null;

		if (label != null)
		{
			String line = label.getText();

			result = StringUtil.getSubstringBetween(line, S_AT, S_SPACE);
		}

		return result;
	}

	public int getIndexForSourceLine(String memberClassName, int sourceIndex)
	{
		int result = -1;

		int pos = 0;
		
		for (Node node : vBoxRows.getChildren())
		{
			Label label = (Label) node;

			String className = getClassNameFromLabel(label);

			if (className != null && className.equals(memberClassName))
			{
				String labelSourceLine = getSourceLineFromLabel(label);

				if (labelSourceLine != null && labelSourceLine.equals(Integer.toString(sourceIndex)))
				{
					result = pos;
					break;
				}
			}

			pos++;
		}
		
		return result;
	}
	
	public int getIndexForBytecodeOffset(String memberClassName, int bytecodeOffset)
	{
		int result = -1;

		int pos = 0;
		
		for (Node node : vBoxRows.getChildren())
		{
			Label label = (Label) node;

			String className = getClassNameFromLabel(label);

			if (className != null && className.equals(memberClassName))
			{
				String labelOffsetLine = getBytecodeOffsetFromLabel(label);

				if (labelOffsetLine != null && labelOffsetLine.equals(Integer.toString(bytecodeOffset)))
				{
					result = pos;
					break;
				}
			}

			pos++;
		}
		
		return result;
	}
}