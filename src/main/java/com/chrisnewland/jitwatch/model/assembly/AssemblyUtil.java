/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model.assembly;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssemblyUtil
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

		String[] lines = asm.split(S_NEWLINE);

		StringBuilder headerBuilder = new StringBuilder();

		AssemblyBlock currentBlock = new AssemblyBlock();

		AssemblyInstruction lastInstruction = null;

		for (String line : lines)
		{
			if (DEBUG_LOGGING)
			{
				logger.debug("line: '{}'", line);
			}

			String trimmedLine = line.replace(S_ENTITY_APOS, S_QUOTE).trim();

			if (trimmedLine.startsWith(S_HASH))
			{
				if (DEBUG_LOGGING)
				{
					logger.debug("Assembly header: {}", trimmedLine);
				}

				headerBuilder.append(trimmedLine).append(S_NEWLINE);
			}
			else if (trimmedLine.startsWith(S_OPEN_SQUARE))
			{
				if (DEBUG_LOGGING)
				{
					logger.debug("new AssemblyBlock: {}", trimmedLine);
				}

				method.addBlock(currentBlock);
				currentBlock = new AssemblyBlock();
				currentBlock.setTitle(trimmedLine);
			}
			else if (trimmedLine.startsWith(S_SEMICOLON))
			{
				if (DEBUG_LOGGING)
				{
					logger.debug("Extended comment? : {}", trimmedLine);
				}

				if (lastInstruction != null)
				{
					if (trimmedLine.length() > 0)
					{
						lastInstruction.addCommentLine(trimmedLine);
					}
				}
			}
			else
			{
				AssemblyInstruction instr = createInstruction(trimmedLine);

				if (instr != null)
				{
					currentBlock.addInstruction(instr);

					lastInstruction = instr;
				}
			}
		}

		method.addBlock(currentBlock);

		method.setHeader(headerBuilder.toString());

		return method;
	}

	public static AssemblyInstruction createInstruction(final String inLine)
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("Trying to parse instruction : {}", inLine);
		}

		String line = inLine;

		AssemblyInstruction instr = null;

		String annotation = S_EMPTY;

		int addressIndex = line.indexOf(S_ASSEMBLY_ADDRESS);

		if (addressIndex != -1)
		{
			annotation = line.substring(0, addressIndex);
			line = line.substring(addressIndex);
		}

		Matcher matcher = PATTERN_ASSEMBLY_INSTRUCTION.matcher(line);

		if (matcher.find() && matcher.groupCount() == 3)
		{
			String address = matcher.group(1);
			String instructionString = matcher.group(2);
			String comment = matcher.group(3);

			if (DEBUG_LOGGING)
			{
				logger.debug(" Annotation: '{}'", annotation);
				logger.debug("    Address: '{}'", address);
				logger.debug("Instruction: '{}'", instructionString);
				logger.debug("    Comment: '{}'", comment);
			}

			long addressValue = 0;

			if (address != null)
			{
				address = address.trim();

				if (address.startsWith(S_ASSEMBLY_ADDRESS))
				{
					address = address.substring(S_ASSEMBLY_ADDRESS.length());
				}

				addressValue = Long.parseLong(address, 16);
			}

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
					StringBuilder modBuilder = new StringBuilder();

					for (int i = 0; i < midParts.length - 2; i++)
					{
						modBuilder.append(midParts[i]).append(S_SPACE);
					}

					modifier = modBuilder.toString().trim();

					mnemonic = midParts[midParts.length - 2];
					opString = midParts[midParts.length - 1];
				}
				else
				{
					logger.error("Don't know how to parse this: {} {}", line, midParts.length);
				}

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

				instr = new AssemblyInstruction(annotation, addressValue, modifier, mnemonic, operands, comment);
			}
		}
		else
		{
			logger.error("Could not parse assembly: {}", line);
		}

		return instr;
	}
}