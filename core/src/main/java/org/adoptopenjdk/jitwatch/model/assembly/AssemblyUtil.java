/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public final class AssemblyUtil
{
	// http://www.delorie.com/djgpp/doc/brennan/brennan_att_inline_djgpp.html
	private static final Logger logger = LoggerFactory.getLogger(AssemblyUtil.class);

	private static final String PART_ADDRESS = "(0x[a-f0-9]+):";
	private static final String PART_INSTRUCTION = "([0-9a-zA-Z:_\\(\\)\\[\\]\\+\\*\\$,\\-%\\s]+)";
	private static final String PART_COMMENT = "([;#].*)?";

	private static final Pattern PATTERN_ASSEMBLY_INSTRUCTION = Pattern
			.compile("^" + PART_ADDRESS + "\\s+" + PART_INSTRUCTION + PART_COMMENT);

	private AssemblyUtil()
	{
	}

	public static AssemblyMethod parseAssembly(final String assemblyString)
	{
		final AssemblyLabels labels = new AssemblyLabels();

		String[] lines = assemblyString.split(S_NEWLINE);

		StringBuilder headerBuilder = new StringBuilder();

		AssemblyBlock currentBlock = new AssemblyBlock();
		currentBlock.setTitle(NATIVE_CODE_ENTRY_POINT);

		AssemblyInstruction lastInstruction = null;

		String lastLine = null;

		AssemblyMethod method = null;

		for (int i = 0; i < lines.length; i++)
		{
			if (DEBUG_LOGGING_ASSEMBLY)
			{
				logger.debug("line: '{}'", lines[i]);
			}

			if (i == 0)
			{
				method = new AssemblyMethod(lines[i]);
			}

			String line = lines[i].replace(S_ENTITY_APOS, S_QUOTE);
			line = line.replaceFirst("^ +", "");

			if (line.startsWith(S_HASH))
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Assembly header: {}", line);
				}

				headerBuilder.append(line).append(S_NEWLINE);
			}
			else if (line.startsWith(S_OPEN_SQUARE))
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("new AssemblyBlock: {}", line);
				}

				method.addBlock(currentBlock);
				currentBlock = new AssemblyBlock();
				currentBlock.setTitle(line);
			}
			else if (line.startsWith(S_SEMICOLON))
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Extended comment? '{}'", line);
				}

				if (lastInstruction != null)
				{
					lastInstruction.addCommentLine(line);
				}
			}
			else
			{
				AssemblyInstruction instr = createInstruction(labels, line);

				if (instr == null && lastLine.trim().startsWith(S_HASH) && !line.startsWith(S_ASSEMBLY_ADDRESS)
						&& !line.contains(' ' + S_ASSEMBLY_ADDRESS))
				{
					// remove last newline
					headerBuilder.setLength(headerBuilder.length() - S_NEWLINE.length());

					headerBuilder.append(line).append(S_NEWLINE);

					// update untrimmedLine since it is used to update
					// lastUntrimmedLine at end of loop
					line = lastLine + line;
				}
				else if (instr == null && lastLine.trim().startsWith(S_SEMICOLON) && lastInstruction != null)
				{
					lastInstruction.appendToLastCommentLine(line);

					// update untrimmedLine since it is used to update
					// lastUntrimmedLine at end of loop
					line = lastLine + line;
				}
				else
				{
					boolean replaceLast = false;
					if (instr == null && i < lines.length - 1)
					{
						// try appending current and next lines together
						String nextUntrimmedLine = lines[i + 1].replace(S_ENTITY_APOS, S_QUOTE);

						instr = createInstruction(labels, line + nextUntrimmedLine);
						if (instr != null)
						{
							i++;
						}
					}

					if (instr == null && lastInstruction != null)
					{
						// try appending last and current lines together
						instr = createInstruction(labels, lastLine + line);
						if (instr != null)
						{
							replaceLast = true;
						}
					}

					if (instr != null)
					{
						if (replaceLast)
						{
							currentBlock.replaceLastInstruction(instr);
						}
						else
						{
							currentBlock.addInstruction(instr);
						}

						lastInstruction = instr;
					}
					else
					{
						logger.error("Could not parse assembly: {}", line);
					}
				}
			}
			lastLine = line;
		}

		method.addBlock(currentBlock);

		method.setHeader(headerBuilder.toString());

		labels.buildLabels();

		return method;
	}

	public static AssemblyInstruction createInstruction(final AssemblyLabels labels, final String inLine)
	{
		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("Trying to parse instruction : {}", inLine);
		}

		String line = inLine;

		AssemblyInstruction instr = null;

		String annotation = S_EMPTY;

		if (!line.startsWith(S_ASSEMBLY_ADDRESS))
		{
			int addressIndex = line.indexOf(' ' + S_ASSEMBLY_ADDRESS);

			if (addressIndex != -1)
			{
				annotation = line.substring(0, addressIndex) + ' ';
				line = line.substring(addressIndex + 1);
			}
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

				long addressValue = getValueFromAddress(address);

				if (instructionString != null)
				{
					instr = parseInstruction(instructionString, addressValue, comment, annotation, labels);
					labels.newInstruction(instr);
				}
			}
		}
		return instr;
	}

	public static AssemblyInstruction parseInstruction(String input, long address, String comment, String annotation,
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
					if ("data32".equals(part))
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

	static long getValueFromAddress(final String address)
	{
		long addressValue = 0;

		if (address != null)
		{
			String trimmedAddress = address.trim();

			if (trimmedAddress.startsWith(S_ASSEMBLY_ADDRESS))
			{
				trimmedAddress = trimmedAddress.substring(S_ASSEMBLY_ADDRESS.length());
			}

			if (trimmedAddress.endsWith(HEXA_POSTFIX))
			{
				trimmedAddress = trimmedAddress.substring(0, trimmedAddress.length() - 1);
			}

			addressValue = Long.parseLong(trimmedAddress, 16);
		}
		return addressValue;
	}
}