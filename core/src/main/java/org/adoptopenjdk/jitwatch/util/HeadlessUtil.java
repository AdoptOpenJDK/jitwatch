/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BYTES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_DECOMPILES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NMSIZE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_STAMP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.HEADLESS_SEPARATOR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HYPEN;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.MetaPackage;

public class HeadlessUtil
{
	public static String modelToString(IReadOnlyJITDataModel model, boolean onlyCompiled)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Package").append(HEADLESS_SEPARATOR);
		builder.append("Class").append(HEADLESS_SEPARATOR);
		builder.append("Member Signature").append(HEADLESS_SEPARATOR);
		builder.append("Is Compiled").append(HEADLESS_SEPARATOR);
		builder.append("Compiler").append(HEADLESS_SEPARATOR);
		builder.append("Queued Time").append(HEADLESS_SEPARATOR);
		builder.append("Compiled Time").append(HEADLESS_SEPARATOR);
		builder.append("Compilation Time").append(HEADLESS_SEPARATOR);
		builder.append("Bytecode Size").append(HEADLESS_SEPARATOR);
		builder.append("Native Size").append(HEADLESS_SEPARATOR);
		builder.append("Decompiles");

		builder.append(S_NEWLINE);

		List<MetaPackage> roots = model.getPackageManager().getRootPackages();

		for (MetaPackage mp : roots)
		{
			showTree(builder, mp, onlyCompiled);
		}

		return builder.toString();
	}

	private static void showTree(StringBuilder builder, MetaPackage mp, boolean onlyCompiled)
	{
		List<MetaPackage> childPackages = mp.getChildPackages();

		for (MetaPackage childPackage : childPackages)
		{
			showTree(builder, childPackage, onlyCompiled);
		}

		List<MetaClass> packageClasses = mp.getPackageClasses();

		for (MetaClass metaClass : packageClasses)
		{
			for (IMetaMember member : metaClass.getMetaMembers())
			{
				boolean isCompiled = member.isCompiled();

				if (!onlyCompiled || isCompiled)
				{
					builder.append(mp.getName()).append(HEADLESS_SEPARATOR);

					builder.append(metaClass.getName()).append(HEADLESS_SEPARATOR);

					builder.append(member.toStringUnqualifiedMethodName(true, true)).append(HEADLESS_SEPARATOR);

					builder.append(isCompiled ? "Y" : "N").append(HEADLESS_SEPARATOR);

					builder.append(getCompiledAttributeOrNA(member, ATTR_COMPILER, S_HYPEN)).append(HEADLESS_SEPARATOR);

					builder.append(getQueuedAttributeOrNA(member, ATTR_STAMP, S_HYPEN)).append(HEADLESS_SEPARATOR);

					builder.append(getCompiledAttributeOrNA(member, ATTR_STAMP, S_HYPEN)).append(HEADLESS_SEPARATOR);

					long lastCompilationTime = getLastCompilationTime(member);

					builder.append(lastCompilationTime).append(HEADLESS_SEPARATOR);

					builder.append(getCompiledAttributeOrNA(member, ATTR_BYTES, S_HYPEN)).append(HEADLESS_SEPARATOR);

					builder.append(getCompiledAttributeOrNA(member, ATTR_NMSIZE, S_HYPEN)).append(HEADLESS_SEPARATOR);

					builder.append(getCompiledAttributeOrNA(member, ATTR_DECOMPILES, "0"));

					builder.append(S_NEWLINE);
				}
			}
		}
	}

	private static long getLastCompilationTime(IMetaMember member)
	{
		long compileTime = 0;

		Compilation lastCompilation = member.getLastCompilation();
		
		if (lastCompilation != null)
		{
			compileTime = lastCompilation.getCompileTime();
		}
		
		return compileTime;
	}

	private static String getQueuedAttributeOrNA(IMetaMember member, String attributeName, String defaultValue)
	{
		String result = defaultValue;

		if (member != null)
		{
			result = getAttributeOrNA(member.getQueuedAttributes(), attributeName, defaultValue);
		}

		return result;
	}

	private static String getCompiledAttributeOrNA(IMetaMember member, String attributeName, String defaultValue)
	{
		String result = defaultValue;

		if (member != null)
		{
			result = getAttributeOrNA(member.getCompiledAttributes(), attributeName, defaultValue);
		}

		return result;
	}

	private static String getAttributeOrNA(Map<String, String> attrs, String attributeName, String defaultValue)
	{
		String result = defaultValue;

		if (attrs.containsKey(attributeName))
		{
			result = attrs.get(attributeName);
		}

		return result;
	}
}