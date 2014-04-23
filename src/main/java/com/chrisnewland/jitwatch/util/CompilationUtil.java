package com.chrisnewland.jitwatch.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.source.util.JavacTask;

public class CompilationUtil
{
	private static final Logger logger = LoggerFactory.getLogger(CompilationUtil.class);

	public static final Path COMPILE_DIR;

	static
	{
		String tempDir = System.getProperty("java.io.tmpdir");

		Path path = null;

		try
		{
			path = Files.createTempDirectory(new File(tempDir).toPath(), "JITWatch_compile_");
		}
		catch (IOException e)
		{
			logger.error("Could not create temp compilation dir", e);
		}

		COMPILE_DIR = path;
	}

	public static void main(String[] args) throws IOException
	{
		File f = writeToFile(new File(COMPILE_DIR.toFile(), "Foo.java"), "public class Foo { public int bar = 42;}");

		List<File> sources = new ArrayList<>();
		sources.add(f);

		compile(sources);
	}

	public static boolean compile(List<File> sourceFiles) throws IOException
	{
		if (COMPILE_DIR == null)
		{
			logger.error("Compile dir has not been created, bailing out");
			return false;
		}

		StringWriter compilerOutputStream = new StringWriter();

		List<String> compileOptions = Arrays.asList(new String[] { "-g", "-d", COMPILE_DIR.toAbsolutePath().toString() });

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

	public static File writeToFile(File f, String str) throws IOException
	{
		BufferedWriter fout = new BufferedWriter(new FileWriter(f));

		try
		{
			fout.write(str);
			fout.flush();
		}
		finally
		{
			fout.close();
		}

		return f;
	}
}
