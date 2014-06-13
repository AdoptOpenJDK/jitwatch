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

	enum ParseState
	{
		OTHER, BYTECODE, LINETABLE
	}

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
			logger.error("Could not obtain bytecode for class: {}", fqClassName, ba);
		}
		catch (IOException ioe)
		{
			logger.error("", ioe);
		}

		if (byteCodeString != null)
		{
			result = parse(byteCodeString);
		}
		
		return result;
	}

	public static ClassBC parse(String bytecode)
	{
		ClassBC classBytecode = new ClassBC();

		String[] lines = bytecode.split(S_NEWLINE);

		int pos = 0;

		StringBuilder builder = new StringBuilder();

		ParseState parseState = ParseState.OTHER;

		String memberSignature = null;

		MemberBytecode memberBytecode = null;

		while (pos < lines.length)
		{
			String line = lines[pos].trim();

			switch (parseState)
			{
			case OTHER:
				if (line.endsWith(");"))
				{
					memberSignature = fixSignature(line);
				}
				else if (line.startsWith("0:"))
				{
					parseState = ParseState.BYTECODE;
					pos--;
				}
				else if (line.startsWith(S_BYTECODE_MINOR_VERSION))
				{
					int minorVersion = getVersionPart(line);
					classBytecode.setMinorVersion(minorVersion);
				}
				else if (line.startsWith(S_BYTECODE_MAJOR_VERSION))
				{
					int majorVersion = getVersionPart(line);
					classBytecode.setMajorVersion(majorVersion);
				}
				break;
			case BYTECODE:
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
						List<BytecodeInstruction> instructions = parseInstructions(builder.toString());

						memberBytecode = new MemberBytecode();
						memberBytecode.setInstructions(instructions);
						builder.delete(0, builder.length());

						if (line.startsWith("LineNumberTable:"))
						{
							parseState = ParseState.LINETABLE;
						}
						else
						{
							classBytecode.addMemberBytecode(memberSignature, memberBytecode);
							parseState = ParseState.OTHER;
						}
					}
				}
				break;
			case LINETABLE:
				if (line.startsWith("line "))
				{
					builder.append(line).append(C_NEWLINE);
				}
				else
				{
					updateLineNumberTable(classBytecode, builder.toString(), memberSignature);
					builder.delete(0, builder.length());

					classBytecode.addMemberBytecode(memberSignature, memberBytecode);
					parseState = ParseState.OTHER;
				}
				break;
			}

			pos++;
		}

		return classBytecode;
	}
	
	private static int getVersionPart(String line)
	{
		int version = 0;
		
		int colonPos = line.indexOf(C_COLON);
		
		if (colonPos != -1 && colonPos != line.length()-1)
		{
			String versionPart = line.substring(colonPos+1).trim();
			
			try
			{
				version = Integer.parseInt(versionPart);
			}
			catch (NumberFormatException nfe)
			{
				logger.error("Could not parse version part {}", versionPart, nfe);
			}
		}
		
		return version;
	}

	private static String fixSignature(String signature)
	{
		String result = null;

		if (signature != null)
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

					result = signature.substring(0, openParentheses) + params + S_CLOSE_PARENTHESES;
				}
			}
		}

		return result;
	}

	public static List<BytecodeInstruction> parseInstructions(String bytecode)
	{
		List<BytecodeInstruction> bytecodeInstructions = new ArrayList<>();

		String[] lines = bytecode.split(S_NEWLINE);

		final Pattern PATTERN_BYTECODE_INSTRUCTION = Pattern
				.compile("^([0-9]+):\\s([0-9a-z_]+)\\s?([#0-9a-z,\\- ]+)?\\s?\\{?\\s?(//.*)?");

		boolean inSwitch = false;
		BCParamSwitch table = new BCParamSwitch();
		BytecodeInstruction instruction = null;

		for (String line : lines)
		{
			line = line.trim();

			if (inSwitch)
			{
				if (S_CLOSE_BRACE.equals(line))
				{
					instruction.addParameter(table);

					bytecodeInstructions.add(instruction);
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
					Matcher matcher = PATTERN_BYTECODE_INSTRUCTION.matcher(line);

					if (matcher.find())
					{
						instruction = new BytecodeInstruction();

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

							bytecodeInstructions.add(instruction);
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

		return bytecodeInstructions;
	}

	private static void updateLineNumberTable(ClassBC classBytecode, String tableLines, String memberSignature)
	{
		String[] lines = tableLines.split(S_NEWLINE);

		for (String line : lines)
		{
			// strip off 'line '
			line = line.trim().substring(5);

			String[] parts = line.split(S_COLON);

			if (parts.length == 2)
			{
				String source = parts[0].trim();
				String offset = parts[1].trim();

				try
				{
					LineTableEntry entry = new LineTableEntry(memberSignature, Integer.parseInt(offset));
					classBytecode.getLineTable().put(Integer.parseInt(source), entry);
				}
				catch (NumberFormatException nfe)
				{
					logger.error("Could not parse line number {}", line, nfe);
				}
			}
			else
			{
				logger.error("Could not split line: {}", line);
			}
		}
	}

	private static void processParameters(String paramString, BytecodeInstruction instruction)
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
