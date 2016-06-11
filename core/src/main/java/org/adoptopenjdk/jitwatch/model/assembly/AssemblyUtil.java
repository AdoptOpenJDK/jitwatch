/*
 * Copyright (c) 2013-2015 Chris Newland.
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

	private static final Pattern PATTERN_ASSEMBLY_INSTRUCTION = Pattern
			.compile("^(0x[a-f0-9]+):\\s+([0-9a-z\\(\\)\\$,\\-%\\s]+)([;#].*)?");

	private AssemblyUtil()
	{
	}

	public static AssemblyMethod parseAssembly(final String asm)
	{
		AssemblyMethod method = new AssemblyMethod();
		final AssemblyLabels labels = new AssemblyLabels();

		String[] lines = asm.split(S_NEWLINE);

		StringBuilder headerBuilder = new StringBuilder();

		AssemblyBlock currentBlock = new AssemblyBlock();

		AssemblyInstruction lastInstruction = null;

		String lastLine = null;
		for (int i = 0; i < lines.length; i++)
		{
			if (DEBUG_LOGGING_ASSEMBLY)
			{
				logger.debug("line: '{}'", lines[i]);
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

				if (instr == null
						&& lastLine.trim().startsWith(S_HASH)
						&& !line.startsWith(S_ASSEMBLY_ADDRESS)
						&& !line.contains(' ' + S_ASSEMBLY_ADDRESS))
				{
					// remove last newline
					headerBuilder.setLength(headerBuilder.length() - S_NEWLINE.length());

					headerBuilder.append(line).append(S_NEWLINE);

					// update untrimmedLine since it is used to update lastUntrimmedLine at end of loop
					line = lastLine + line;
				}
				else if (instr == null
						&& lastLine.trim().startsWith(S_SEMICOLON)
						&& lastInstruction != null)
				{
					lastInstruction.appendToLastCommentLine(line);

					// update untrimmedLine since it is used to update lastUntrimmedLine at end of loop
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
						if (instr != null) {
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

	public static AssemblyInstruction createInstruction(
		final AssemblyLabels labels, final String inLine)
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

		if (matcher.find() && matcher.groupCount() == 3)
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

			String modifier = null;
			String mnemonic = null;
			List<String> operands = new ArrayList<>();

			if (instructionString != null)
			{
				String[] midParts = instructionString.trim().split(S_REGEX_WHITESPACE);

				// mnemonic
				// mnemonic operands
				// modifier mnemonic operands

				String opString = null;

				if (midParts.length == 1)
				{
					mnemonic = midParts[0];
				}
				else if (midParts.length == 2)
				{
					mnemonic = midParts[0];
					opString = midParts[1];
				}
				else if (midParts.length >= 3)
				{
					modifier = getModForThreeOrMoreOperands(midParts);

					mnemonic = midParts[midParts.length - 2];
					opString = midParts[midParts.length - 1];
				}
				else
				{
					logger.error("Don't know how to parse this: {} {}", line, midParts.length);
				}

				addValidOperandsToList(operands, opString);

				instr = new AssemblyInstruction(annotation, addressValue, modifier, mnemonic, operands, comment, labels);
				labels.newInstruction(instr);
			}
		}

		return instr;
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

	private static void addValidOperandsToList(List<String> operands, String opString)
	{
		if (opString != null)
		{
			StringBuilder opBuilder = new StringBuilder();

			// can't tokenise on comma because
			// address operand such as 0x0(%rax,%rax,1)
			// is a single parameter
			boolean inParentheses = false;

			for (int pos = 0; pos < opString.length(); pos++)
			{
				char c = opString.charAt(pos);

				if (c == C_OPEN_PARENTHESES)
				{
					inParentheses = true;
					opBuilder.append(c);
				}
				else if (c == C_CLOSE_PARENTHESES)
				{
					inParentheses = false;
					opBuilder.append(c);
				}
				else if (c == C_COMMA && !inParentheses)
				{
					String operand = opBuilder.toString();
					opBuilder.delete(0, opBuilder.length());
					operands.add(operand);
				}
				else
				{
					opBuilder.append(c);
				}
			}

			if (opBuilder.length() > 0)
			{
				String operand = opBuilder.toString();
				opBuilder.delete(0, opBuilder.length() - 1);
				operands.add(operand);
			}
		}
	}

	private static String getModForThreeOrMoreOperands(String[] midParts)
	{
		StringBuilder modBuilder = new StringBuilder();

		for (int i = 0; i < midParts.length - 2; i++)
		{
			modBuilder.append(midParts[i]).append(S_SPACE);
		}

		return modBuilder.toString().trim();
	}
}