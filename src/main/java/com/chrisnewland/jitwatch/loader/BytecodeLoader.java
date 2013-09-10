package com.chrisnewland.jitwatch.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.tools.javap.JavapTask;
import com.sun.tools.javap.JavapTask.BadArgs;

public class BytecodeLoader
{
	public static Map<String, String> fetchByteCodeForClass(List<String> classLocations, String fqClassName)
	{
		String[] args;

		if (classLocations.size() == 0)
		{
			args = new String[] { "-c", "-p", fqClassName };
		}
		else
		{
			StringBuilder classPathBuilder = new StringBuilder();

			for (String cp : classLocations)
			{
				classPathBuilder.append(cp).append(File.pathSeparatorChar);
			}

			classPathBuilder.deleteCharAt(classPathBuilder.length() - 1);

			args = new String[] { "-c", "-p", "-classpath", classPathBuilder.toString(), fqClassName };
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try
		{
			JavapTask task = new JavapTask();
			task.setLog(baos);
			task.handleOptions(args);
			task.call();
		}
		catch (BadArgs ba)
		{
			System.out.println("Could not obtain bytcode for class: " + fqClassName);
		}

		String result = new String(baos.toByteArray());

		return parse(result);

	}

	private static Map<String, String> parse(String result)
	{
		String[] lines = result.split("\n");

		int pos = 0;

		String signature = null;
		StringBuilder builder = new StringBuilder();

		boolean inMethod = false;

		Map<String, String> bytecodeMap = new HashMap<>();

		while (pos < lines.length)
		{
			String line = lines[pos].trim();

			if (inMethod)
			{
				if (line.length() == 0)
				{
					inMethod = false;
					storeBytecode(bytecodeMap, signature, builder);
				}
				else
				{
					builder.append(line).append("\n");
				}
			}
			else
			{
				if (line.startsWith("Code:") && pos > 0)
				{
					signature = lines[pos - 1].trim();
					signature = signature.substring(0, signature.length() - 1);
					inMethod = true;
				}
			}

			pos++;
		}

		storeBytecode(bytecodeMap, signature, builder);

		return bytecodeMap;
	}

	private static void storeBytecode(Map<String, String> bytecodeMap, String signature, StringBuilder builder)
	{
		if (signature != null && builder.length() > 0)
		{
			// remove spaces between multiple method parameters

			int openParentheses = signature.lastIndexOf("(");

			if (openParentheses != -1)
			{
				int closeParentheses = signature.indexOf(")", openParentheses);

				if (closeParentheses != -1)
				{
					String params = signature.substring(openParentheses, closeParentheses);
					params = params.replace(" ", "");

					signature = signature.substring(0, openParentheses) + params + signature.substring(closeParentheses);
				}
			}

			bytecodeMap.put(signature, builder.toString());
			builder.delete(0, builder.length());
		}
	}
}