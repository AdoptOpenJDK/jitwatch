/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.demo;

import com.chrisnewland.jitwatch.loader.BytecodeLoader;
import com.chrisnewland.jitwatch.model.bytecode.ClassBC;
import com.chrisnewland.jitwatch.model.bytecode.BytecodeInstruction;
import com.chrisnewland.jitwatch.model.bytecode.MemberBytecode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarScan
{

    private static final int SIX_CHARACTERS_WIDE = 6;
    private static final int MAX_METHOD_SIZE_IN_BYTES = 325;

    /*
                Hide Utility Class Constructor
                Utility classes should not have a public or default constructor.
            */
    private JarScan() {
    }

    @SuppressWarnings("unchecked")
	public static void iterateJar(File jarFile, int maxMethodBytes, PrintWriter writer) throws IOException
	{
		List<String> classLocations = new ArrayList<>();

		classLocations.add(jarFile.getPath());

		try (ZipFile zip = new ZipFile(jarFile))
		{
			Enumeration<ZipEntry> list = (Enumeration<ZipEntry>) zip.entries();

			while (list.hasMoreElements())
			{
				ZipEntry entry = list.nextElement();

				String name = entry.getName();

				if (name.endsWith(".class"))
				{
					String fqName = name.replace("/", ".").substring(0, name.length() - SIX_CHARACTERS_WIDE);

					process(classLocations, fqName, maxMethodBytes, writer);
				}
			}
		}
	}

	private static void process(List<String> classLocations, String className, int maxMethodBytes, PrintWriter writer)
	{
		ClassBC classBytecode = BytecodeLoader.fetchBytecodeForClass(classLocations, className);

		boolean shownClass = false;

		for (String memberName : classBytecode.getBytecodeMethodSignatures())
		{			
			MemberBytecode memberBytecode = classBytecode.getMemberBytecode(memberName);
			
			List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

			if (instructions != null && instructions.size() > 0)
			{
				BytecodeInstruction lastInstruction = instructions.get(instructions.size() - 1);

				// assume final instruction is a return of some kind for 1 byte
				int bcSize = 1 + lastInstruction.getOffset();

				if (bcSize >= maxMethodBytes && !memberName.equals("static {}"))
				{
					if (!shownClass)
					{
						writer.println(className);
						shownClass = true;
					}

					writer.print(bcSize);
					writer.print(" -> ");
					writer.println(memberName);
					writer.flush();
				}
			}
		}
	}

	public static void main(String[] args) throws IOException
	{
		int maxMethodBytes = Integer.getInteger("maxMethodSize", MAX_METHOD_SIZE_IN_BYTES);

		PrintWriter writer = new PrintWriter(System.out);

		for (String jar : args)
		{
			File jarFile = new File(jar);

			writer.print(jarFile.getAbsolutePath());

			writer.println(':');

			iterateJar(jarFile, maxMethodBytes, writer);

			writer.println();
		}

		writer.flush();
		writer.close();
	}
}
