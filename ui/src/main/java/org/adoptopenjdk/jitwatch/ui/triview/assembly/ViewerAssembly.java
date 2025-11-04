/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview.assembly;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_AT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOLLAR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HEX_PREFIX;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HEX_POSTFIX;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import javafx.scene.input.Mnemonic;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyBlock;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyReference;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyUtil;
import org.adoptopenjdk.jitwatch.model.assembly.IAssemblyParser;
import org.adoptopenjdk.jitwatch.model.assembly.Architecture;
import org.adoptopenjdk.jitwatch.model.assembly.arm.*;
import org.adoptopenjdk.jitwatch.model.assembly.x86.AssemblyParserX86;
import org.adoptopenjdk.jitwatch.model.assembly.x86.X86RegisterType;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener.LineType;
import org.adoptopenjdk.jitwatch.ui.triview.Viewer;
import org.adoptopenjdk.jitwatch.util.StringUtil;

// https://github.com/yasm/yasm/wiki/GasSyntax
// http://x86asm.net/articles/what-i-dislike-about-gas/
// http://vaskoz.wordpress.com/2013/07/14/hotspot-disassembler/
// https://sourceware.org/binutils/docs/as/i386_002dRegs.html#i386_002dRegs

public class ViewerAssembly extends Viewer
{
	private DecimalFormat formatThousandsUnderscore;

	private IAssemblyParser parser; // TODO choose parser

	public ViewerAssembly(IStageAccessProxy stageAccessProxy, ILineListener lineListener, LineType lineType)
	{
		super(stageAccessProxy, lineListener, lineType, true);

		formatThousandsUnderscore = (DecimalFormat) NumberFormat.getInstance();

		DecimalFormatSymbols symbols = formatThousandsUnderscore.getDecimalFormatSymbols();

		symbols.setGroupingSeparator('_');

		formatThousandsUnderscore.setDecimalFormatSymbols(symbols);
	}

	public void setAssemblyMethod(AssemblyMethod asmMethod, boolean showLocalLabels)
	{
		parser = AssemblyUtil.getParserForArchitecture(asmMethod.getArchitecture());

		lastScrollIndex = -1;

		List<Label> labels = new ArrayList<>();

		int annoWidth = asmMethod.getMaxAnnotationWidth();

		String annoPad = StringUtil.repeat(C_SPACE, annoWidth);

		String header = asmMethod.getHeader();

		if (header != null)
		{
			String[] headerLines = header.split(S_NEWLINE);

			for (String headerLine : headerLines)
			{
				labels.add(createLabel(annoPad + headerLine));
			}
		}

		for (AssemblyBlock block : asmMethod.getBlocks())
		{
			String title = block.getTitle();

			if (title != null)
			{
				labels.add(createLabel(annoPad + title));
			}

			for (final AssemblyInstruction instr : block.getInstructions())
			{
				List<String> commentLines = instr.getCommentLines();

				if (commentLines.size() == 0)
				{
					Label lblLine = createLabel(instr, annoWidth, 0, showLocalLabels);
					lblLine.setTooltip(new Tooltip(getToolTip(instr)));
					labels.add(lblLine);
				}
				else
				{
					for (int i = 0; i < commentLines.size(); i++)
					{
						Label lblLine = createLabel(instr, annoWidth, i, showLocalLabels);
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

		String mnemonic = instruction.getMnemonic();
		String ref = "";

		Architecture arch = null;
		if (parser instanceof AssemblyParserARM)
		{
			arch = ((AssemblyParserARM) parser).architecture; // this could be either ARM64 or ARM32
		} else if (parser instanceof AssemblyParserX86)
		{
			arch = Architecture.X86_64;
		}

		// Lookup with proper architecture
		ref = AssemblyReference.lookupMnemonic(mnemonic, arch);

		if (ref == null)
		{
			String instructionline = instruction.toString().split(":")[1].trim().split(";")[0].trim();
			MnemonicEntry info = AssemblyReference.lookupMnemonicInfo(instructionline, arch);
			if (info != null) {
				ref = info.brief;
			}
		}

		if (mnemonic != null && mnemonic.startsWith("."))
		{
			ref = AssemblyReference.lookupDirective(mnemonic, arch);
		}

		if (ref == null)
		{
			ref = "Unknown instruction. Assembly reference loaded?";
		}

		builder.append(ref).append(S_NEWLINE);

		List<String> operands = instruction.getOperands();

		// AT&T = source, dest
		// Intel = dest, source
		int pos = 1;

		for (String operand : operands)
		{
			builder.append("operand ").append(pos).append(": ");

			decodeOperand(mnemonic, operand, builder);

			builder.append(S_NEWLINE);

			pos++;
		}

		return builder.toString();
	}

	private void decodeOperand(String mnemonic, String operand, StringBuilder builder)
	{
		operand = operand.trim(); // trim away any leading/trailing whitespaces
		if (parser.isRegister(mnemonic, operand))
		{
			builder.append(decodeRegister(operand));
		}
		else if (parser.isConstant(mnemonic, operand))
		{
			builder.append(getConstantLabel(operand));
		}
		else if (parser.isAddress(mnemonic, operand))
		{
			builder.append("Address ");
			builder.append(operand);
		} else if (parser.isShift(mnemonic, operand))
		{
			builder.append(getShiftLabel(operand));
		} else if (parser.isExtend(mnemonic, operand)) {
			builder.append(getExtendLabel(operand));
		}
	}

	private String getConstantLabel(String operand)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Constant ");
		builder.append(operand);

		try
		{
			if (operand.startsWith(S_DOLLAR))
			{
				operand = operand.substring(1);
			}

			if (operand.endsWith(S_HEX_POSTFIX))
			{
				operand = S_HEX_PREFIX + operand.substring(0, operand.length() - 1);
			}

			long decimal = Long.decode(operand);

			builder.append(S_SPACE).append(S_OPEN_PARENTHESES).append("Decimal: ").append(formatThousandsUnderscore.format(decimal))
					.append(S_CLOSE_PARENTHESES);
		}
		catch (NumberFormatException nfe)
		{
			// e.g. $0xffffffffffffffff - ignore it
		}

		return builder.toString();
	}

	private String decodeRegister(String input)
	{
		StringBuilder builder = new StringBuilder();

		// https://sourceware.org/binutils/docs/as/i386_002dRegs.html#i386_002dRegs
		// http://www.x86-64.org/documentation/assembly.html

		String regName = parser.extractRegisterName(input);


		if ((parser != null) && (parser.getArchitecture() == Architecture.ARM_64))
		{
			ARMRegisterType regType = ARMRegisterType.fromRegisterName(regName);
			builder.append(regType.getDescription()).append(" ").append(regName);
			builder.append(ARMRegisterType.getSpecialPurpose(regName));
		}
		else
		{
			X86RegisterType regType = X86RegisterType.fromRegisterName(regName);
			builder.append(regType.getDescription()).append(" ").append(regName);
			builder.append(X86RegisterType.getSpecialPurpose(regName));
		}

		return builder.toString();
	}

	private String getShiftLabel(String operand)
	{
		if (operand == null || operand.isEmpty()) return "";

		StringBuilder builder = new StringBuilder();
		String[] shifterParts = operand.split("\\s+");
		ARMShifter shifter = ARMShifter.fromString(shifterParts[0]);

		if (shifter != null)
		{
			builder.append("Shift ").append(shifter.name())
					.append(S_SPACE + S_OPEN_PARENTHESES).append(shifter.getDescription()).append(S_CLOSE_PARENTHESES);

			// check for immediate values
			if (shifterParts.length > 1)
			{
				builder.append(" - ").append(getConstantLabel(shifterParts[1]));
			}
		}

		return builder.toString();
	}

	private String getExtendLabel(String operand)
	{
		if (operand == null || operand.isEmpty()) return "";

		StringBuilder builder = new StringBuilder();
		String[] extendParts = operand.split("\\s+");
		ARMExtend extend = ARMExtend.fromString(extendParts[0]);

		if (extend != null)
		{
			builder.append("Extend ").append(extend.name())
					.append(S_SPACE + S_OPEN_PARENTHESES).append(extend.getDescription()).append(S_CLOSE_PARENTHESES);

			if (extendParts.length > 1)
			{
				builder.append(" - ").append(getConstantLabel(extendParts[1]));
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

	private AssemblyLabel createLabel(AssemblyInstruction instruction, int annoWidth, int line, boolean showLocalLabels)
	{
		AssemblyLabel lbl = new AssemblyLabel(instruction, annoWidth, line, showLocalLabels);

		if (instruction.isSafePoint())
		{
			lbl.setUnhighlightedStyle(STYLE_SAFEPOINT);
		}

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

	public int getIndexForBytecodeOffset(String memberClassName, BytecodeInstruction bcInstruction)
	{
		int result = -1;
		int pos = 0;

		boolean isInvoke = bcInstruction.getOpcode().isInvoke();
		int bytecodeOffset = bcInstruction.getOffset();

		boolean lastAssemblyIsCall = false;

		for (Node node : vBoxRows.getChildren())
		{
			Label label = (Label) node;

			if (label instanceof AssemblyLabel)
			{
				AssemblyInstruction lastAssemblyInstruction = ((AssemblyLabel) label).getInstruction();

				String mnemonic = lastAssemblyInstruction.getMnemonic();

				if (mnemonic != null && mnemonic.toLowerCase().startsWith("call"))
				{
					lastAssemblyIsCall = true;
				}
				else
				{
					lastAssemblyIsCall = false;
				}
			}

			String className = getClassNameFromLabel(label);

			if (className != null && className.equals(memberClassName))
			{
				String labelOffsetLine = getBytecodeOffsetFromLabel(label);

				if (labelOffsetLine != null && labelOffsetLine.equals(Integer.toString(bytecodeOffset)))
				{
					if (isInvoke && lastAssemblyIsCall)
					{
						result = pos;
						break;
					}
					else if (result == -1)
					{
						result = pos;
					}
				}
			}

			pos++;
		}

		return result;
	}
}