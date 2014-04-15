/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.loader;

import com.chrisnewland.jitwatch.model.bytecode.*;
import com.sun.tools.javap.JavapTask;
import com.sun.tools.javap.JavapTask.BadArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public final class BytecodeLoader
{
    private static final Logger logger = LoggerFactory.getLogger(BytecodeLoader.class);

	public static ClassBC fetchBytecodeForClass(Collection<String> classLocations, String fqClassName)
	{
		ClassBC result = null;

		String[] args;

		if (classLocations.size() == 0)
		{
			args = new String[] { "-c", "-p", "-v", fqClassName };
		}
		else
		{
			StringBuilder classPathBuilder = new StringBuilder();

			for (String cp : classLocations)
			{
				classPathBuilder.append(cp).append(File.pathSeparatorChar);
			}

			classPathBuilder.deleteCharAt(classPathBuilder.length() - 1);

			args = new String[] { "-c", "-p", "-v", "-classpath", classPathBuilder.toString(), fqClassName };
		}

		String byteCodeString = null;

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(65536))
		{
			JavapTask task = new JavapTask();
			task.setLog(baos);
			task.handleOptions(args);
			task.call();

			byteCodeString = baos.toString();
		}
		catch (BadArgs ba)
		{
			logger.error("Could not obtain bytecode for class: " + fqClassName, ba);
		}
		catch (IOException ioe)
		{
            logger.error("{}", ioe);
		}

		if (byteCodeString != null)
		{
			result = parse(byteCodeString);
		}

		return result;
	}

	private static ClassBC parse(String result)
	{
		ClassBC classBytecode = new ClassBC();

		String[] lines = result.split(S_NEWLINE);

		int pos = 0;

		String signature = null;
		StringBuilder builder = new StringBuilder();

		boolean inMethod = false;

		while (pos < lines.length)
		{
			String line = lines[pos].trim();

			if (inMethod)
			{
				int firstColonIndex = line.indexOf(C_COLON);

				if (firstColonIndex != -1)
				{
					String beforeColon = line.substring(0, firstColonIndex);

					try
					{
						// line number ?
						Integer.parseInt(beforeColon);

						builder.append(line).append(C_NEWLINE);
					}
					catch (NumberFormatException nfe)
					{
						inMethod = false;
						storeBytecode(classBytecode, signature, builder);
					}
				}
			}
			else
			{
				if (line.startsWith("Code:") && pos >= 2)
				{
					for (int i = 1; i <= 3; i++)
					{
						signature = lines[pos - i].trim();

						if (signature.indexOf(C_COLON) == -1)
						{
							break;
						}
					}

					signature = signature.substring(0, signature.length() - 1);
					inMethod = true;
					pos++; // skip over stack info
				}
			}

			pos++;
		}

		storeBytecode(classBytecode, signature, builder);

		return classBytecode;
	}

	private static void storeBytecode(ClassBC classBytecode, String inSignature, StringBuilder builder)
	{
        String signature = inSignature;
		if (signature != null && builder.length() > 0)
		{
			// remove spaces between multiple method parameters

			int openParentheses = signature.lastIndexOf(S_OPEN_PARENTHESES);

			if (openParentheses != -1)
			{
				int closeParentheses = signature.indexOf(S_CLOSE_PARENTHESES, openParentheses);

				if (closeParentheses != -1)
				{
					String params = signature.substring(openParentheses, closeParentheses);
					params = params.replace(S_SPACE, S_EMPTY);

					signature = signature.substring(0, openParentheses) + params + signature.substring(closeParentheses);
				}
			}

			List<Instruction> instructions = parseInstructions(builder.toString());

			classBytecode.addMemberBytecode(signature, instructions);

			builder.delete(0, builder.length());
		}
	}

	public static List<Instruction> parseInstructions(String bytecode)
	{
		List<Instruction> result = new ArrayList<>();

		String[] lines = bytecode.split(S_NEWLINE);

		final Pattern PATTERN_LOG_SIGNATURE = Pattern.compile("^([0-9]+):\\s([0-9a-z_]+)\\s?([#0-9a-z,\\- ]+)?\\s?\\{?\\s?(//.*)?");

		boolean inSwitch = false;
		BCParamSwitch table = new BCParamSwitch();
		Instruction instruction = null;

		for (String line : lines)
		{
			line = line.trim();

			if (inSwitch)
			{
				if (S_CLOSE_BRACE.equals(line))
				{
					instruction.addParameter(table);

					result.add(instruction);
					inSwitch = false;
				}
				else
				{
					String[] parts = line.split(S_COLON);

					if (parts.length == 2)
					{
						table.put(parts[0].trim(), parts[1].trim());
					}
					else
					{
                        logger.error("Unexpected tableswitch entry: " + line);
					}
				}
			}
			else
			{
				try
				{
					Matcher matcher = PATTERN_LOG_SIGNATURE.matcher(line);

					if (matcher.find())
					{
						instruction = new Instruction();

						String offset = matcher.group(1);
						String mnemonic = matcher.group(2);
						String paramString = matcher.group(3);
						String comment = matcher.group(4);

						instruction.setOffset(Integer.parseInt(offset));
						instruction.setOpcode(Opcode.getOpcodeForMnemonic(mnemonic));

						if (comment != null && comment.trim().length() > 0)
						{
							instruction.setComment(comment.trim());
						}

						if (instruction.getOpcode() == Opcode.TABLESWITCH || instruction.getOpcode() == Opcode.LOOKUPSWITCH)
						{
							inSwitch = true;
						}
						else
						{
							if (paramString != null && paramString.trim().length() > 0)
							{
								processParameters(paramString.trim(), instruction);
							}

							result.add(instruction);
						}
					}
					else
					{
                        logger.error("could not parse bytecode: '" + line + "'");
					}
				}
				catch (Exception e)
				{
                    logger.error("Error parsing bytecode line: '" + line + "'", e);
				}
			}
		}

		return result;
	}

	private static void processParameters(String paramString, Instruction instruction)
	{
		String[] parts = paramString.split(S_COMMA);

		for (String part : parts)
		{
			IBytecodeParam parameter;

			part = part.trim();

			if (part.charAt(0) == C_HASH)
			{
				parameter = new BCParamConstant(part);
			}
			else
			{
				try
				{
					int value = Integer.parseInt(part);
					parameter = new BCParamNumeric(value);
				}
				catch (NumberFormatException nfe)
				{
					parameter = new BCParamString(part);
				}
			}

			instruction.addParameter(parameter);
		}
	}
}
