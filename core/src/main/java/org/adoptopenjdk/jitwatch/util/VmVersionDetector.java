/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import org.adoptopenjdk.jitwatch.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmVersionDetector
{
	private static final Logger logger = LoggerFactory.getLogger(VmVersionDetector.class);

	public static int getMajorVersionFromHotSpotTag(Tag tagVmVersion)
	{
		int result = 0;

		if (tagVmVersion != null)
		{
			Tag tagInfo = tagVmVersion.getFirstNamedChild("info");

			Tag tagRelease = tagVmVersion.getFirstNamedChild("release");

			String releaseText = tagRelease != null ? tagRelease.getTextContent().trim() : null;

			if (tagInfo != null)
			{
				String infoText = tagInfo.getTextContent().trim();

				String vmVersion = StringUtil.getSubstringBetween(infoText, "JRE (", ")");

				if (vmVersion.startsWith("1.7.0"))
				{
					result = 7;
				}
				else if (vmVersion.startsWith("1.8.0"))
				{
					result = 8;
				}
				else
				{
					StringBuilder builder = new StringBuilder();

					boolean seenDigit = false;

					for (int i = 0; i < vmVersion.length(); i++)
					{
						char c = vmVersion.charAt(i);

						if (Character.isDigit(c))
						{
							seenDigit = true;
							builder.append(c);
						}
						else if (seenDigit)
						{
							break;
						}
					}

					try
					{
						result = Integer.parseInt(builder.toString());
					}
					catch (NumberFormatException nfe)
					{
						logger.warn("Could not determine JDK version from log: {}", vmVersion);
					}
				}
			}
		}

		return result;
	}
}
