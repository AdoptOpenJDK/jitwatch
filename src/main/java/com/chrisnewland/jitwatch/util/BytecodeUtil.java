/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.bytecode.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class BytecodeUtil
{
	private static Map<String, String> bcDescriptionMap = new HashMap<>();

	private static final String JVMS_HTML_FILENAME = "JVMS.html";
	private static final String JVMS_CSS_FILENAME = "JVMS.css";

    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private BytecodeUtil() {
    }

    public static boolean hasLocalJVMS()
	{
		File file = new File(JVMS_HTML_FILENAME);

		return file.exists();
	}

	public static boolean isJVMSLoaded()
	{
		return bcDescriptionMap.size() > 0;
	}

	public static String getJVMSCSSURL()
	{
		File cssFile = new File(JVMS_CSS_FILENAME);

		if (cssFile.exists())
		{
			return cssFile.toURI().toString();
		}
		else
		{
			return null;
		}
	}

	public static boolean fetchJVMS()
	{
		// System.out.println("fetchJVMS");

		String html = NetUtil.fetchURL("http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html");
		String css = NetUtil.fetchURL("http://docs.oracle.com/javase/specs/javaspec.css");

		boolean result = false;

		if (html.length() > 0 && css.length() > 0)
		{
			Path pathHTML = Paths.get(new File(JVMS_HTML_FILENAME).toURI());
			Path pathCSS = Paths.get(new File(JVMS_CSS_FILENAME).toURI());

			try
			{
				Files.write(pathHTML, html.getBytes(StandardCharsets.UTF_8));
				Files.write(pathCSS, css.getBytes(StandardCharsets.UTF_8));

				result = true;
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}

		return result;
	}

	public static void loadJVMS()
	{
		// System.out.println("loadJVMS");
		try
		{
			Path path = Paths.get(new File(JVMS_HTML_FILENAME).toURI());

			String html = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

			int htmlLength = html.length();

			String descStart = "<div class=\"section-execution\"";

			int startPos = html.indexOf(descStart);

			while (startPos != -1 && startPos < htmlLength)
			{
				int endPos = html.indexOf(descStart, startPos + descStart.length());

				if (endPos != -1)
				{
					String desc = html.substring(startPos, endPos);
					storeBytecodeDescription(desc);
					startPos = endPos;
				}
				else if (startPos != -1)
				{
					String desc = html.substring(startPos);
					storeBytecodeDescription(desc);
					break;
				}
				else
				{
					break;
				}
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	private static void storeBytecodeDescription(String description)
	{
		String title = StringUtil.getSubstringBetween(description, "<div class=\"section-execution\" title=\"", S_DOUBLE_QUOTE);

		if (title != null)
		{
			// System.out.println("store: " + title);

			bcDescriptionMap.put(title, description);
		}
	}

	public static String getBytecodeDescriptions(Opcode opcode)
	{
		String opcodeText = opcode.getText();

		// System.out.println("lookup: " + opcodeText);

		String desc = bcDescriptionMap.get(opcodeText);

		if (desc == null)
		{
			// System.out.println("null: " + opcodeText);

			for (Map.Entry<String, String> entry : bcDescriptionMap.entrySet())
			{
				String key = entry.getKey();

				int ltPos = key.indexOf(C_OPEN_ANGLE);

				// ifge => if<cond>
				// lconst_1 => lconst_<n>
				if (ltPos != -1)
				{
					if (ltPos < opcodeText.length())
					{
						String subOpcodeText = opcodeText.substring(0, ltPos);
						String subKey = key.substring(0, ltPos);

						// System.out.println("checking: " + subOpcodeText + "/"
						// + subKey);

						if (subOpcodeText.equals(subKey))
						{
							// System.out.println("subs: " + key);

							desc = entry.getValue();
							break;
						}
					}
				}
			}
		}

		// if (desc == null)
		// {
		// System.out.println("nothing found");
		// }

		return desc;
	}

	public static String getBytecodeForMember(IMetaMember member, List<String> classLocations)
	{
		String bytecodeSignature = member.getSignatureForBytecode();
		Map<String, String> bytecodeCache = member.getMetaClass().getBytecodeCache(classLocations);

		String result = bytecodeCache.get(bytecodeSignature);

		if (result == null)
		{
			List<String> keys = new ArrayList<>(bytecodeCache.keySet());

			bytecodeSignature = ParseUtil.findBestMatchForMemberSignature(member, keys);

			if (bytecodeSignature != null)
			{
				result = bytecodeCache.get(bytecodeSignature);
			}
		}

		return result;
	}

	public static List<Instruction> parseInstructions(String bytecode)
	{
		List<Instruction> result = new ArrayList<>();

		String[] lines = bytecode.split(S_NEWLINE);

		Pattern PATTERN_LOG_SIGNATURE = Pattern.compile("^([0-9]+):\\s([0-9a-z_]+)\\s?([#0-9a-z,\\- ]+)?\\s?(//.*)?");

		for (String line : lines)
		{
			try
			{
				Matcher matcher = PATTERN_LOG_SIGNATURE.matcher(line);

				if (matcher.find())
				{
					Instruction instruction = new Instruction();

					String offset = matcher.group(1);
					String mnemonic = matcher.group(2);
					String paramString = matcher.group(3);
					String comment = matcher.group(4);

					instruction.setOffset(Integer.parseInt(offset));
					instruction.setMnemonic(mnemonic);

					if (paramString != null)
					{
						processParameters(paramString, instruction);
					}

					if (comment != null)
					{
						instruction.setComment(comment.trim());
					}

					result.add(instruction);

				}
				else
				{
					System.out.println("could not parse bytecode: " + line);
				}
			}
			catch (Exception e)
			{
				System.out.println("Error parsing line: " + line);
				e.printStackTrace();
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
				catch(NumberFormatException nfe)
				{
					parameter = new BCParamString(part);
				}
			}
			
			instruction.addParameter(parameter);
		}
	}
}