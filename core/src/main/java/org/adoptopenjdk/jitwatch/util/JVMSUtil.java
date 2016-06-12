/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOUBLE_QUOTE;

public final class JVMSUtil
{
	private static final Logger logger = LoggerFactory.getLogger(JVMSUtil.class);

	private static Map<String, String> bcDescriptionMap = new HashMap<>();

	private static final String JVMS_HTML_FILENAME = "JVMS.html";
	private static final String JVMS_CSS_FILENAME = "JVMS.css";

	private JVMSUtil()
	{
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
				logger.error("Could not save JVMS to disk", ioe);
			}
		}

		return result;
	}

	public static void loadJVMS()
	{
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
			logger.error("", ioe);
		}
	}

	private static void storeBytecodeDescription(String description)
	{
		String title = StringUtil.getSubstringBetween(description, "<div class=\"section-execution\" title=\"", S_DOUBLE_QUOTE);

		if (title != null)
		{
			bcDescriptionMap.put(title, description);
		}
	}

	public static String getBytecodeDescriptions(Opcode opcode)
	{
		String opcodeText = opcode.getMnemonic();

		String desc = bcDescriptionMap.get(opcodeText);

		if (desc == null)
		{
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

						if (subOpcodeText.equals(subKey))
						{
							desc = entry.getValue();
							break;
						}
					}
				}
			}
		}

		return desc;
	}

}