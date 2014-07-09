package org.adoptopenjdk.jitwatch.sandbox;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class ClassCompiler
{
	private String compilationMessages;

	public boolean compile(List<File> sourceFiles, File outputDir) throws IOException
	{
		StringWriter compilerOutputStream = new StringWriter();

		List<String> compileOptions = Arrays.asList(new String[] { "-g", "-d", outputDir.getAbsolutePath().toString() });

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
}
