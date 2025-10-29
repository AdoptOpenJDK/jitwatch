/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly.arm;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_CLOSE_SQUARE_BRACKET;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_SQUARE_BRACKET;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_ASSEMBLY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HEX_PREFIX;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.adoptopenjdk.jitwatch.model.assembly.AbstractAssemblyParser;
import org.adoptopenjdk.jitwatch.model.assembly.Architecture;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyLabels;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyUtil;

public class AssemblyParserARM extends AbstractAssemblyParser
{
	private static final String PART_ADDRESS = "(" + S_HEX_PREFIX + "[a-f0-9]+):";
	private static final String PART_INSTRUCTION = "([0-9a-zA-Z:_\\(\\)\\[\\]{}\\+\\*\\$#,\\-%\\s]+)";
	private static final String PART_COMMENT = "([;].*)?";

	private static final Pattern ASSEMBLY_CONSTANT = Pattern
			.compile("^#[0-9]+$");

	private static final Pattern PATTERN_ASSEMBLY_INSTRUCTION = Pattern
			.compile("^" + PART_ADDRESS + "\\s+" + PART_INSTRUCTION + PART_COMMENT);

	public final Architecture architecture;

	public AssemblyParserARM(Architecture architecture)
	{
		super(architecture);
		this.architecture = architecture;
	}
	
	@Override
	public AssemblyInstruction createInstruction(final AssemblyLabels labels, final String inLine)
	{
		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("Trying to parse instruction : {}", inLine);
		}

		String line = inLine;

		AssemblyInstruction instr = null;

		String annotation = S_EMPTY;

		if (!line.startsWith(S_HEX_PREFIX))
		{
			int addressIndex = line.indexOf(' ' + S_HEX_PREFIX);

			if (addressIndex != -1)
			{
				annotation = line.substring(0, addressIndex) + ' ';
				line = line.substring(addressIndex + 1);
			}
		}

		// now let's display those directives
		if (line.matches(S_HEX_PREFIX + "[a-f0-9]+:\\s+\\..*"))
		{
			// Directive with an address
			String address = line.substring(0, line.indexOf(':'));
			String directiveAndComment = line.substring(line.indexOf(':') + 1).trim();

			String comment = null;
			if (directiveAndComment.contains(";"))
			{
				comment = directiveAndComment.substring(directiveAndComment.indexOf(';')).trim();
				directiveAndComment = directiveAndComment.substring(0, directiveAndComment.indexOf(';')).trim();
			}

			// Split directive into parts
			String[] parts = directiveAndComment.trim().split("\\s+", 2);
			String directive = parts[0]; // e.g., ".inst"
			String operand = parts.length > 1 ? parts[1] : ""; // e.g., "0x8cc91c30"

			long addressValue = AssemblyUtil.getValueFromAddress(address);

			// Create instruction with the directive as the mnemonic
			List<String> operands = new ArrayList<>();
			if (!operand.isEmpty()) {
				operands.add(operand);
			}

			instr = new AssemblyInstruction(annotation, addressValue,
					new ArrayList<>(), directive,
					operands, comment, labels);
			labels.newInstruction(instr);
			return instr;
		}

		Matcher matcher = PATTERN_ASSEMBLY_INSTRUCTION.matcher(line);

		if (matcher.find())

		{
			if (DEBUG_LOGGING_ASSEMBLY)
			{
				for (int i = 1; i <= matcher.groupCount(); i++)
				{
					logger.debug("parts : '{}'='{}'", i, matcher.group(i));
				}
			}

			if (matcher.groupCount() == 3)
			{
				String address = matcher.group(1);
				String instructionString = matcher.group(2);
				String comment = matcher.group(3);

				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Annotation : '{}'", annotation);
					logger.debug("Address    : '{}'", address);
					logger.debug("Instruction: '{}'", instructionString);
					logger.debug("Comment    : '{}'", comment);
				}

				long addressValue = AssemblyUtil.getValueFromAddress(address);

				if (instructionString != null && instructionString.trim().length() > 0)
				{
					instr = parseInstruction(instructionString, addressValue, comment, annotation, labels);
					labels.newInstruction(instr);
				}
			}
		}
				
		return instr;
	}

	@Override
	public AssemblyInstruction parseInstruction(String input, long address, String comment, String annotation,
			AssemblyLabels labels)
	{
		input = input.replaceAll("\\s+", S_SPACE).trim();

		int length = input.length();

		boolean inBrackets = false;

		String mnemonic = null;

		List<String> prefixes = new ArrayList<>();
		List<String> operands = new ArrayList<>();

		StringBuilder partBuilder = new StringBuilder();

		for (int pos = 0; pos < length; pos++)
		{
			char c = input.charAt(pos);

			if (c == C_OPEN_PARENTHESES || c == C_OPEN_SQUARE_BRACKET)
			{
				inBrackets = true;
			}
			else if (c == C_CLOSE_PARENTHESES || c == C_CLOSE_SQUARE_BRACKET)
			{
				inBrackets = false;
			}

			if (c == C_SPACE && mnemonic == null)
			{
				// end of part
				String part = partBuilder.toString();

				partBuilder.delete(0, partBuilder.length());

				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("part: '{}'", part);
				}

				if (mnemonic == null)
				{
					if ("data64".equals(part) || "data32".equals(part) || "data16".equals(part) || "data8".equals(part)
							|| "lock".equals(part))
					{
						prefixes.add(part);
					}
					else
					{
						mnemonic = part;
						if (DEBUG_LOGGING_ASSEMBLY)
						{
							logger.debug("mnemonic: '{}'", mnemonic);
						}
					}
				}
			}
			else if (c == C_COMMA && !inBrackets)
			{
				String operand = partBuilder.toString();
				partBuilder.delete(0, partBuilder.length());
				operands.add(operand);

				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("operand1: '{}'", operand);
				}
			}
			else
			{
				partBuilder.append(c);
			}
		}

		if (partBuilder.length() > 0)
		{
			String part = partBuilder.toString();
			partBuilder.delete(0, partBuilder.length() - 1);

			if (mnemonic == null)
			{
				mnemonic = part;
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("mnemonic: '{}'", part);
				}
			}
			else
			{
				operands.add(part);
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("operand2: '{}'", part);
				}
			}
		}

		return new AssemblyInstruction(annotation, address, prefixes, mnemonic, operands, comment, labels);
	}

	@Override
	public boolean isConstant(String mnemonic, String operand)
	{
		if (operand == null || isJump(mnemonic)) return false;
    
		// Match ARM-style immediates with the # prefix
		if (architecture == Architecture.ARM_64 || architecture == Architecture.ARM_32) {
			String value = operand.startsWith("#") ? operand.substring(1) : operand;
			
			return value.matches("-?[0-9]+") || value.matches(S_HEX_PREFIX + "[0-9a-fA-F]+");
		}

		return ASSEMBLY_CONSTANT.matcher(operand).find() && !isJump(mnemonic);
	}

	@Override
	public boolean isRegister(String mnemonic, String operand)
	{
		if (operand == null) return false; 

		// after the first operand, we should trim additional whitespaces
		operand = operand.trim();

		// aarch64 has X and W registers to represent the different sf flag options
		if (architecture == Architecture.ARM_64)
		{
			// simple registers that go from x0-x30 & w0-w30, sp, zero registers (x and w), and vector registers
			if ((operand.matches("(?i)^[xw][0-9]{1,2}$")) || (operand.equals("sp")) || (operand.matches("(?i)^(xzr|wzr)$")) || (operand.matches("(?i)^[vbhsdq][0-9]{1,2}$")))
			{
				return true;
			}

			// checks the "bracket" notation of the registers
			if (operand.matches("(?i)^\\[.*\\]!?$") || operand.matches("(?i)^\\[.*\\],\\s*#.*$")) return true; // memory addressing in ARM assembly [reg] or [reg, #offset]

			// If multiple registers without any brackets, then split by comma
			String[] parts = operand.split(",");
			for (String part : parts) {
				part = part.trim();

				// Accept simple registers but only set to false if we encounter a not good example
				if (!(part.matches("(?i)^[xw][0-9]{1,2}$") || part.equalsIgnoreCase("sp") ||
						part.equalsIgnoreCase("xzr") || part.equalsIgnoreCase("wzr") ||
						part.matches("(?i)^[vbhsdq][0-9]{1,2}$") ||
						part.matches("(?i)^\\[.*\\]!?$") || part.matches("(?i)^\\[.*\\],\\s*#.*$") ||
						part.matches("(?i)^(lsl|lsr|asr|ror|rrx)\\s*#?[0-9]+$"))) {

					return false;
				}
			}

			return true; // if everything went through (mainly after the loop ends)
		}

		// aarch32 has a simplified register system
		else if (architecture == Architecture.ARM_32) 
		{
			if ((operand.matches("(?i)^(sp|lr|pc)$")) || (operand.matches("(?i)^r[0-9]{1,2}$"))) return true; // either r0-r15 match OR the special registers match

			// multi-part entry
			String[] parts = operand.split(",");
			for (String part : parts) {
				part = part.trim();
				if (!(part.matches("(?i)^(sp|lr|pc)$") ||
						part.matches("(?i)^r[0-9]{1,2}$") ||
						part.matches("(?i)^(lsl|lsr|asr|ror|rrx)\\s*#?[0-9]+$"))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isJump(String mnemonic)
	{
		boolean result = false;

		if (mnemonic != null)
		{
			result = mnemonic.toLowerCase().startsWith("b") || mnemonic.toLowerCase().startsWith("call");
		}

		return result;
	}

	@Override
	public String extractRegisterName(String input)
	{
		if (input == null) return null;

		// also remove leading/trailing spaces for the input
		input = input.trim();

		String regName = input;

		int indexOpenParentheses = input.indexOf(C_OPEN_PARENTHESES);
		int indexCloseParentheses = input.indexOf(C_CLOSE_PARENTHESES);

		if (indexOpenParentheses != -1 && indexCloseParentheses != -1) regName = regName.substring(indexOpenParentheses + 1, indexCloseParentheses);

		// square brackets -- common in ARM addressing modes
		int indexOpenSquareBracket = regName.indexOf(C_OPEN_SQUARE_BRACKET);
		int indexCloseSquareBracket = regName.indexOf(C_CLOSE_SQUARE_BRACKET);

		if (indexOpenSquareBracket != -1 && indexCloseSquareBracket != -1)
		{
			regName = input.substring(indexOpenSquareBracket + 1, indexCloseSquareBracket).trim(); // base register first
			if (regName.contains(",")) regName = regName.substring(0, regName.indexOf(',')).trim();
		}

		// immediate values -- obtain just the integer value
		if (regName.startsWith("#")) regName = regName.substring(1);

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < regName.length(); i++)
		{
			char c = regName.charAt(i);

			if (Character.isAlphabetic(c) || Character.isDigit(c))
			{
				builder.append(c);
			}
			else
			{
				break;
			}
		}

		regName = builder.toString();

		if (isRegister(null, regName))
		{
			return regName;
		}

		return null;
	}
}