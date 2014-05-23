/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model.assembly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class AssemblyUtil
{
    //http://www.delorie.com/djgpp/doc/brennan/brennan_att_inline_djgpp.html
	private static final Logger logger = LoggerFactory.getLogger(AssemblyUtil.class);

	private static final Pattern PATTERN_ASSEMBLY_INSTRUCTION = Pattern
			.compile("^([a-f0-9x]+):\\s+([0-9a-z\\(\\)\\$,\\-%\\s]+)([;#].*)?");
    private static final int COUNT_OF_THREE = 3;
    private static final int ADDRESS_GROUP = 1;
    private static final int MIDDLE_GROUP = 2;
    private static final int COMMENT_GROUP = 3;
    private static final int RADIX_OF_SIXTEEN = 16;
    private static final int ONE_PART = 1;
    private static final int TWO_PARTS = 2;
    private static final int THREE_PARTS = 3;

    private AssemblyUtil()
	{
	}

	public static AssemblyMethod parseAssembly(String asm)
	{
		AssemblyMethod method = new AssemblyMethod();

		String[] lines = asm.split(S_NEWLINE);

		StringBuilder headerBuilder = new StringBuilder();

		AssemblyBlock currentBlock = new AssemblyBlock();

		AssemblyInstruction lastInstruction = null;

		for (String line : lines)
		{
			line = line.trim();

			if (line.startsWith(S_HASH))
			{
				headerBuilder.append(line).append(S_NEWLINE);
			}
			else if (line.startsWith(S_OPEN_SQUARE))
			{
				method.addBlock(currentBlock);
				currentBlock = new AssemblyBlock();
				currentBlock.setTitle(line);
			}
			else if (line.startsWith(S_ASSEMBLY_ADDRESS))
			{
				AssemblyInstruction instr = createInstruction(line);

				currentBlock.addInstruction(instr);

				lastInstruction = instr;
			}
			else
			{
				// extended comment
				if (lastInstruction != null)
				{
					if (line.length() > 0)
					{
						lastInstruction.addCommentLine(line);
					}
				}
				else
				{
					logger.error("Found comment but lastInstruction is null: {}", line);
				}
			}
		}

		method.addBlock(currentBlock);

		method.setHeader(headerBuilder.toString());

		return method;
	}

	public static AssemblyInstruction createInstruction(String line)
	{
		AssemblyInstruction instr = null;

		Matcher matcher = PATTERN_ASSEMBLY_INSTRUCTION.matcher(line);

		if (matcher.find() && matcher.groupCount() == COUNT_OF_THREE)
		{
			String address = matcher.group(ADDRESS_GROUP);
			String middle = matcher.group(MIDDLE_GROUP);
			String comment = matcher.group(COMMENT_GROUP);

			long addressValue = 0;

			if (address != null)
			{
				address = address.trim();

				if (address.startsWith(S_ASSEMBLY_ADDRESS))
				{
					address = address.substring(S_ASSEMBLY_ADDRESS.length());
				}

				addressValue = Long.parseLong(address, RADIX_OF_SIXTEEN);
			}

			String modifier = null;
			String mnemonic = null;
			List<String> operands = new ArrayList<>();

			if (middle != null)
			{
				String[] midParts = middle.trim().split(S_REGEX_WHITESPACE);

				// mnemonic
				// mnemonic operands
				// modifier mnemonic operands

				String opString = null;

				if (midParts.length == ONE_PART)
				{
					mnemonic = midParts[0];
				}
				else if (midParts.length == TWO_PARTS)
				{
					mnemonic = midParts[0];
					opString = midParts[1];
				}
				else if (midParts.length >= THREE_PARTS)
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

				instr = new AssemblyInstruction(addressValue, modifier, mnemonic, operands, comment);
			}
		}
		else
		{
			logger.error("Could not parse assembly: {}", line);
		}

		return instr;
	}
}