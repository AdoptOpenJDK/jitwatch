/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.process.AbstractProcess;

public class CompilerScala extends AbstractProcess implements ICompiler
{
	private Path compilerPath;
	
	private String languageHomeDir;

	private final String COMPILER_NAME = "scalac";

	public CompilerScala(String languageHomeDir) throws FileNotFoundException
	{
		super();

		this.languageHomeDir = languageHomeDir;
		
		compilerPath = Paths.get(languageHomeDir, "bin", COMPILER_NAME);

		if (!compilerPath.toFile().exists())
		{
			throw new FileNotFoundException("Could not find " + COMPILER_NAME);
		}

		compilerPath = compilerPath.normalize();
	}

	@Override
	public boolean compile(List<File> sourceFiles, List<String> classpathEntries, File outputDir, ILogListener logListener)
			throws IOException
	{
		List<String> commands = new ArrayList<>();

		commands.add(compilerPath.toString());

		String outputDirPath = outputDir.getAbsolutePath().toString();

		List<String> compileOptions = Arrays.asList(new String[] { "-print", "-g:vars", "-d", outputDirPath });

		commands.addAll(compileOptions);
		
		addScalaCoreLibs(classpathEntries);

		if (classpathEntries.size() > 0)
		{
			commands.add("-classpath");

			commands.add(makeClassPath(classpathEntries));
		}

		for (File sourceFile : sourceFiles)
		{
			commands.add(sourceFile.getAbsolutePath());
		}

		boolean success = runCommands(commands, logListener);
		
		if (success)
		{
			String realSource = getOutputStream();
					
			/*
			FileWriter writer = null;

			try
			{
				writer = new FileWriter(saveFile);
				writer.write(getSource());
				sandboxStage.log("Saved " + saveFile.getCanonicalPath());
				
				sourceFile = saveFile;
			}
			catch (IOException ioe)
			{
				sandboxStage.log("Could not save file");
			}
			finally
			{
				if (writer != null)
				{
					try
					{
						writer.close();
					}
					catch (IOException ioe)
					{
					}
				}
			}
			*/
			
		}
		
		return success;
	}
	
	private void addScalaCoreLibs(List<String> classpathEntries)
	{
		String[] coreLibs = Paths.get(languageHomeDir, "lib").toFile().list(new FilenameFilter()
		{
			
			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".jar");
			}
		});
		
		for (String filename : coreLibs)
		{
			if (!classpathEntries.contains(filename))
			{
				classpathEntries.add(filename);
			}
		}		
	}
}