package com.chrisnewland.jitwatch.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import com.sun.source.util.JavacTask;

public class CompilationUtil
{
	private static final Logger logger = LoggerFactory.getLogger(CompilationUtil.class);

	public static final Path SANDBOX_SOURCE_DIR;
	public static final Path SANDBOX_CLASS_DIR;

	static
	{
		String userDir = System.getProperty("user.dir");

		File sandbox = new File(userDir, "sandbox");
		File sandboxSources = new File(sandbox, "sources");
		File sandboxClasses = new File(sandbox, "classes");

		if (!sandboxSources.exists())
		{
			sandboxSources.mkdirs();
		}

		if (!sandboxClasses.exists())
		{
			sandboxClasses.mkdirs();
		}

		SANDBOX_SOURCE_DIR = sandboxSources.toPath();
		SANDBOX_CLASS_DIR = sandboxClasses.toPath();
	}

	public static boolean compile(List<File> sourceFiles) throws IOException
	{
		if (SANDBOX_SOURCE_DIR == null || SANDBOX_CLASS_DIR == null)
		{
			logger.error("Compile dirs have not been created, bailing out");
			return false;
		}

		StringWriter compilerOutputStream = new StringWriter();

		List<String> compileOptions = Arrays.asList(new String[] { "-g", "-d", SANDBOX_CLASS_DIR.toAbsolutePath().toString() });

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();

		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);

		Iterable<? extends JavaFileObject> sourceFileObjects = fileManager.getJavaFileObjectsFromFiles(sourceFiles);

		JavaCompiler.CompilationTask task = compiler.getTask(compilerOutputStream, fileManager, null, compileOptions, null,
				sourceFileObjects);

		JavacTask javacTask = (JavacTask) task;

		boolean success = javacTask.call();

		String compilationMessages = compilerOutputStream.toString();

		logger.info("Compilation Success: {}", success);

		if (compilationMessages.length() > 0)
		{
			logger.info("Compilation Message: {}", compilationMessages);
		}

		return success;
	}

	public static File writeSource(String fqClassName, String sourceCode) throws IOException
	{
		String[] parts = fqClassName.split(S_BACKSLASH + S_DOT);

		StringBuilder builder = new StringBuilder();

		builder.append(SANDBOX_SOURCE_DIR).append(File.separatorChar);
		
		for (String part : parts)
		{
			logger.info("part: {}", part);
			builder.append(part).append(File.separatorChar);
		}
		
		builder.deleteCharAt(builder.length() - 1);

		builder.append(".java");
		
		String filePathString = builder.toString();
		
		int lastSep = filePathString.lastIndexOf(File.separatorChar);
		
		File sourceFile;
		
		if (lastSep != -1)
		{
			String dirPart = filePathString.substring(0, lastSep);
			String filePart = filePathString.substring(lastSep+1);
			File dir = new File(dirPart);
			
			if (!dir.exists())
			{
				dir.mkdirs();
			}
			
			sourceFile = new File(dir, filePart);
		}
		else
		{
			sourceFile = new File(filePathString);
		}
		
		logger.info("Writing source file: {}", sourceFile.getAbsolutePath());

		BufferedWriter fout = new BufferedWriter(new FileWriter(sourceFile));

		try
		{
			fout.write(sourceCode);
			fout.flush();
		}
		finally
		{
			fout.close();
		}

		return sourceFile;
	}
}
