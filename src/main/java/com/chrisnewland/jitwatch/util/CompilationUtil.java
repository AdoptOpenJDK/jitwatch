package com.chrisnewland.jitwatch.util;

import com.sun.source.util.JavacTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class CompilationUtil
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

    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private CompilationUtil() {
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
