/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.loader;

import com.sun.tools.javap.JavapTask;
import com.sun.tools.javap.JavapTask.BadArgs;

import org.adoptopenjdk.jitwatch.model.bytecode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public final class BytecodeLoader
{
	private static final Logger logger = LoggerFactory.getLogger(BytecodeLoader.class);

    private static final Pattern PATTERN_BYTECODE_INSTRUCTION = Pattern
            .compile("^([0-9]+):\\s([0-9a-z_]+)\\s?([#0-9a-z,\\- ]+)?\\s?\\{?\\s?(//.*)?");

	enum BytecodeSection
	{
		NONE, CONSTANT_POOL, CODE, EXCEPTIONS, LINETABLE, RUNTIMEVISIBLEANNOTATIONS, LOCALVARIABLETABLE, STACKMAPTABLE
	}

	private static final Map<String, BytecodeSection> sectionLabelMap = new HashMap<>();

	static
	{
		sectionLabelMap.put(S_BYTECODE_CONSTANT_POOL, BytecodeSection.CONSTANT_POOL);
		sectionLabelMap.put(S_BYTECODE_CODE, BytecodeSection.CODE);
		sectionLabelMap.put(S_BYTECODE_LINENUMBERTABLE, BytecodeSection.LINETABLE);
		sectionLabelMap.put(S_BYTECODE_LOCALVARIABLETABLE, BytecodeSection.LOCALVARIABLETABLE);
		sectionLabelMap.put(S_BYTECODE_RUNTIMEVISIBLEANNOTATIONS, BytecodeSection.RUNTIMEVISIBLEANNOTATIONS);
		sectionLabelMap.put(S_BYTECODE_EXCEPTIONS, BytecodeSection.EXCEPTIONS);
		sectionLabelMap.put(S_BYTECODE_STACKMAPTABLE, BytecodeSection.STACKMAPTABLE);
	}
	/*
	 * Hide Utility Class Constructor Utility classes should not have a public
	 * or default constructor.
	 */

    private BytecodeLoader()
    {}

	public static ClassBC fetchBytecodeForClass(Collection<String> classLocations, String fqClassName)
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("fetchBytecodeForClass: {}", fqClassName);
		}

		String[] args = buildClassPathFromClassLocations(classLocations, fqClassName);

        String byteCodeString  = createJavapTaskFromArguments(fqClassName, args);

        return parsedByteCodeFrom(byteCodeString);
	}

    private static ClassBC parsedByteCodeFrom(String byteCodeString) {
        ClassBC result = null;
        if (byteCodeString != null)
		{
			try
			{
				result = parse(byteCodeString);
			}
			catch (Exception ex)
			{
				logger.error("Exception parsing bytecode", ex);
			}
		}
        return result;
    }

    private static String createJavapTaskFromArguments(String fqClassName, String[] args) {
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
        return byteCodeString;
    }

    private static String[] buildClassPathFromClassLocations(Collection<String> classLocations, String fqClassName) {
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
        return args;
    }

    // TODO refactor this class - better stateful than all statics
	public static ClassBC parse(String bytecode)
	{
		ClassBC classBytecode = new ClassBC();

		String[] lines = bytecode.split(S_NEWLINE);

		int pos = 0;

		StringBuilder builder = new StringBuilder();

		BytecodeSection section = BytecodeSection.NONE;

		String memberSignature = null;

		MemberBytecode memberBytecode = null;

		while (pos < lines.length)
		{
			String line = lines[pos].trim();

			if (DEBUG_LOGGING)
			{
				logger.debug("Line: {}", line);
			}

			BytecodeSection nextSection = getNextSection(line);

			if (nextSection != null)
			{
				sectionFinished(section, memberSignature, builder, memberBytecode, classBytecode);

				section = changeSection(nextSection);
				pos++;

				if (pos < lines.length)
				{
					line = lines[pos].trim();
				}
			}

			if (DEBUG_LOGGING)
			{
				logger.debug("{} Line: {}", section, line);
			}

			switch (section)
			{
			case NONE:
				if (couldBeMemberSignature(line))
				{
					memberSignature = cleanBytecodeMemberSignature(line);

					if (DEBUG_LOGGING)
					{
						logger.debug("New signature: {}", memberSignature);
					}

					memberBytecode = new MemberBytecode();

					if (DEBUG_LOGGING)
					{
						logger.debug("Initialised new MemberBytecode");
					}
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
			case CODE:
                section = performCODE(classBytecode, builder, section, memberSignature, memberBytecode, line);
				break;
			case CONSTANT_POOL:
                section = performConstantPool(classBytecode, builder, section, memberSignature, memberBytecode, line);
                break;
			case LINETABLE:
                section = performLINETABLE(classBytecode, builder, section, memberSignature, memberBytecode, line);
                break;
			case RUNTIMEVISIBLEANNOTATIONS:
				if (!isRunTimeVisibleAnnotation(line))
				{
					section = changeSection(BytecodeSection.NONE);
					pos--;
				}
				break;
			case LOCALVARIABLETABLE:
				if (!isLocalVariableLine(line))
				{
					section = changeSection(BytecodeSection.NONE);
					pos--;
				}
				break;
			case STACKMAPTABLE:
				if (!isStackMapTable(line))
				{
					section = changeSection(BytecodeSection.NONE);
					pos--;
				}
				break;
			case EXCEPTIONS:
				break;
			}

			pos++;
		}

		return classBytecode;
	}

    private static BytecodeSection performLINETABLE(ClassBC classBytecode, StringBuilder builder, BytecodeSection section, String memberSignature, MemberBytecode memberBytecode, String line) {
        if (line.startsWith("line "))
        {
            builder.append(line).append(C_NEWLINE);
        }
        else
        {
            sectionFinished(BytecodeSection.LINETABLE, memberSignature, builder, memberBytecode, classBytecode);

            section = changeSection(BytecodeSection.NONE);
        }
        return section;
    }

    private static BytecodeSection performConstantPool(ClassBC classBytecode, StringBuilder builder, BytecodeSection section, String memberSignature, MemberBytecode memberBytecode, String line) {
        if (!line.startsWith(S_HASH))
        {
            sectionFinished(BytecodeSection.CONSTANT_POOL, memberSignature, builder, memberBytecode, classBytecode);

            section = changeSection(BytecodeSection.NONE);
        }
        return section;
    }

    private static BytecodeSection performCODE(ClassBC classBytecode, StringBuilder builder, BytecodeSection section, String memberSignature, MemberBytecode memberBytecode, String line) {
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
                sectionFinished(BytecodeSection.CODE, memberSignature, builder, memberBytecode, classBytecode);

                section = changeSection(BytecodeSection.NONE);
            }
        }
        return section;
    }

    private static boolean isRunTimeVisibleAnnotation(final String line)
	{
		return line.contains(": #");
	}

	private static boolean isLocalVariableLine(final String line)
	{
		return line.startsWith("Start") || (line.length() > 0 && Character.isDigit(line.charAt(0)));
	}

	private static boolean isStackMapTable(final String line)
	{
		String trimmedLine = line.trim();
		return trimmedLine.startsWith("frame_type") || trimmedLine.startsWith("offset_delta") || trimmedLine.startsWith("locals")
				|| trimmedLine.startsWith("stack");
	}

	private static boolean couldBeMemberSignature(String line)
	{
		return line.endsWith(");") || line.contains(" throws ") && line.endsWith(S_SEMICOLON)
				|| line.startsWith(S_BYTECODE_STATIC_INITIALISER_SIGNATURE);
	}

	private static void sectionFinished(BytecodeSection lastSection, String memberSignature, StringBuilder builder,
			MemberBytecode memberBytecode, ClassBC classBytecode)
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("sectionFinished: {}", lastSection);
		}

		if (lastSection == BytecodeSection.CODE)
		{
			List<BytecodeInstruction> instructions = parseInstructions(builder.toString());

			memberBytecode.setInstructions(instructions);

			classBytecode.putMemberBytecode(memberSignature, memberBytecode);

			if (DEBUG_LOGGING)
			{
				logger.debug("stored bytecode for : {}", memberSignature);
			}

		}
		else if (lastSection == BytecodeSection.LINETABLE)
		{
			updateLineNumberTable(classBytecode, builder.toString(), memberSignature);

			if (DEBUG_LOGGING)
			{
				logger.debug("stored line number table for : {}", memberSignature);
			}
		}

		builder.delete(0, builder.length());
	}

	private static BytecodeSection changeSection(BytecodeSection nextSection)
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("Changing section to: {}", nextSection);
		}

		return nextSection;
	}

	private static BytecodeSection getNextSection(final String line)
	{
		BytecodeSection nextSection = null;

		if (line != null)
		{
			if (line.length() == 0)
			{
				nextSection = BytecodeSection.NONE;
			}

			for (Map.Entry<String, BytecodeSection> entry : sectionLabelMap.entrySet())
			{
				if (entry.getKey().startsWith(line.trim()))
				{
					nextSection = entry.getValue();
					break;
				}
			}
		}

		return nextSection;
	}

	private static int getVersionPart(final String line)
	{
		int version = 0;

		int colonPos = line.indexOf(C_COLON);

		if (colonPos != -1 && colonPos != line.length() - 1)
		{
			String versionPart = line.substring(colonPos + 1).trim();

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

	private static String cleanBytecodeMemberSignature(final String signature)
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("cleanBytecodeMemberSignature: {}", signature);
		}

		String result = null;

		if (signature != null)
		{
			if (signature.startsWith(S_BYTECODE_STATIC_INITIALISER_SIGNATURE))
			{
				result = S_BYTECODE_STATIC_INITIALISER_SIGNATURE;
			}
			else
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
		}

		return result;
	}



	public static List<BytecodeInstruction> parseInstructions(final String bytecode)
	{
		List<BytecodeInstruction> bytecodeInstructions = new ArrayList<>();

		String[] lines = bytecode.split(S_NEWLINE);


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
