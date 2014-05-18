package com.chrisnewland.jitwatch.sandbox;

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

public class ClassCompiler
{
	private static final Logger logger = LoggerFactory.getLogger(ClassCompiler.class);

	public static final Path SANDBOX_DIR;
	public static final Path SANDBOX_SOURCE_DIR;
	public static final Path SANDBOX_CLASS_DIR;
	
	private String compilationMessages;

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

		SANDBOX_DIR = sandbox.toPath();
		SANDBOX_SOURCE_DIR = sandboxSources.toPath();
		SANDBOX_CLASS_DIR = sandboxClasses.toPath();
	}

	public boolean compile(List<File> sourceFiles) throws IOException
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

		boolean success = task.call();

		compilationMessages = compilerOutputStream.toString();

		return success;
	}
	
	public String getCompilationMessages()
	{
		return compilationMessages;
	}

	public static File writeSource(String fqClassName, String sourceCode) throws IOException
	{
		String[] parts = fqClassName.split(S_BACKSLASH + S_DOT);

		StringBuilder builder = new StringBuilder();

		builder.append(SANDBOX_SOURCE_DIR).append(File.separatorChar);
		
		for (String part : parts)
		{
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
