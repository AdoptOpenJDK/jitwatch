/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.sandbox;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.ILogParser;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig.CompressedOops;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig.TieredCompilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.sandbox.compiler.CompilerJava;
import org.adoptopenjdk.jitwatch.sandbox.compiler.CompilerScala;
import org.adoptopenjdk.jitwatch.sandbox.compiler.ICompiler;
import org.adoptopenjdk.jitwatch.sandbox.runtime.IRuntime;
import org.adoptopenjdk.jitwatch.sandbox.runtime.RuntimeJava;
import org.adoptopenjdk.jitwatch.sandbox.runtime.RuntimeScala;
import org.adoptopenjdk.jitwatch.ui.sandbox.ISandboxStage;
import org.adoptopenjdk.jitwatch.util.FileUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sandbox
{
	private static final Logger logger = LoggerFactory.getLogger(Sandbox.class);

	private ISandboxLogListener logListener;
	private ISandboxStage sandboxStage;

	public static final Path SANDBOX_DIR;
	public static final Path SANDBOX_SOURCE_DIR;
	public static final Path SANDBOX_CLASS_DIR;

	private static final String SANDBOX_LOGFILE = "sandbox.log";

	private File sandboxLogFile = new File(SANDBOX_DIR.toFile(), SANDBOX_LOGFILE);

	private ILogParser logParser;

	static
	{
		String userDir = System.getProperty("user.dir");

		SANDBOX_DIR = Paths.get(userDir, "sandbox");
		SANDBOX_SOURCE_DIR = Paths.get(userDir, "sandbox", "sources");
		SANDBOX_CLASS_DIR = Paths.get(userDir, "sandbox", "classes");

		initialise();
	}

	private static void initialise()
	{
		File sandboxSources = SANDBOX_SOURCE_DIR.toFile();

		if (!sandboxSources.exists())
		{
			logger.debug("Creating Sandbox source directory {}", sandboxSources);

			sandboxSources.mkdirs();

			if (sandboxSources.exists())
			{
				copyExamples();
			}
		}

		File sandboxClasses = SANDBOX_CLASS_DIR.toFile();

		if (!sandboxClasses.exists())
		{
			sandboxClasses.mkdirs();
		}
	}

	public void reset()
	{
		logger.debug("Resetting Sandbox to default settings");
		FileUtil.emptyDir(SANDBOX_DIR.toFile());
		initialise();
	}

	private static void copyExamples()
	{

		File srcDir = new File("src/main/resources/examples");
		File dstDir = SANDBOX_SOURCE_DIR.toFile();

		logger.debug("Copying Sandbox examples from {} to {}", srcDir, dstDir);

		FileUtil.copyFilesToDir(srcDir, dstDir);
	}

	public Sandbox(ILogParser parser, ISandboxLogListener logger, ISandboxStage sandboxStage)
	{
		this.logParser = parser;
		this.logListener = logger;
		this.sandboxStage = sandboxStage;
	}

	private ICompiler getCompiler(String language, ISandboxLogListener logger)
	{
		ICompiler compiler = null;

		String compilerPath = logParser.getConfig().getVMLanguageCompilerPath(language);

		if (compilerPath != null && !S_EMPTY.equals(compilerPath))
		{
			logListener.log("Compiler path: " + compilerPath);

			switch (language)
			{
			case VM_LANGUAGE_JAVA:
				compiler = new CompilerJava(compilerPath);
				break;
			case VM_LANGUAGE_SCALA:
				compiler = new CompilerScala(compilerPath);
				break;
			}
		}

		return compiler;
	}

	private IRuntime getRuntime(String language, ISandboxLogListener logger)
	{
		IRuntime runtime = null;

		String runtimePath = logParser.getConfig().getVMLanguageRuntimePath(language);

		if (runtimePath != null && !S_EMPTY.equals(runtimePath))
		{
			logListener.log("Runtime path: " + runtimePath);

			switch (language)
			{
			case VM_LANGUAGE_JAVA:
				runtime = new RuntimeJava(runtimePath);
				break;
			case VM_LANGUAGE_SCALA:
				runtime = new RuntimeScala(runtimePath);
				break;
			}
		}

		return runtime;
	}

	public void runSandbox(String language, List<File> compileList, File fileToRun) throws Exception
	{
		logListener.log("Running Sandbox");
		logListener.log("Language is " + language);

		ICompiler compiler = getCompiler(language, logListener);

		if (compiler == null)
		{
			logListener.log(language + " compiler path not set. Please click Configure Sandbox and set up the path.");
			return;
		}

		IRuntime runtime = getRuntime(language, logListener);

		if (runtime == null)
		{
			logListener.log(language + " runtime path not set. Please click Configure Sandbox and set up the path.");
			return;
		}

		logListener.log("Compiling: " + StringUtil.listToString(compileList));

		boolean compiledOK = compiler.compile(compileList, logParser.getConfig().getClassLocations(), SANDBOX_CLASS_DIR.toFile(),
				logListener);

		logListener.log("Compilation success: " + compiledOK);

		if (compiledOK)
		{
			String fqClassNameToRun = runtime.getClassToExecute(fileToRun);

			boolean executionSuccess = executeClass(fqClassNameToRun, runtime, logParser.getConfig().isSandboxIntelMode());

			logListener.log("Execution success: " + executionSuccess);

			if (executionSuccess)
			{
				runJITWatch();

				if (!logParser.hasParseError())
				{
					String fqClassNameForTriView = runtime.getClassForTriView(fileToRun);

					showTriView(language, fqClassNameForTriView);
				}
			}
			else
			{
				sandboxStage.showError(runtime.getErrorStream());
			}
		}
		else
		{
			sandboxStage.showError(compiler.getErrorStream());
		}
	}

	/*
	 * private File writeSourceFile(String source) throws IOException { String
	 * sourcePackage = ParseUtil.getPackageFromSource(source);
	 * 
	 * String sourceClass = ParseUtil.getClassFromSource(source);
	 * 
	 * StringBuilder fqNameSourceBuilder = new StringBuilder();
	 * 
	 * if (sourcePackage.length() > 0) {
	 * fqNameSourceBuilder.append(sourcePackage).append(S_DOT); }
	 * 
	 * fqNameSourceBuilder.append(sourceClass);
	 * 
	 * String fqNameSource = fqNameSourceBuilder.toString();
	 * 
	 * if (source.contains("public static void main(") ||
	 * source.contains("public static void main (")) { classContainingMain =
	 * fqNameSource; logListener.log("Found main method in " +
	 * classContainingMain); }
	 * 
	 * if (firstClassName == null) { firstClassName = fqNameSource; }
	 * 
	 * logListener.log("Writing source file: " + fqNameSource + ".java");
	 * 
	 * return FileUtil.writeSource(SANDBOX_SOURCE_DIR.toFile(), fqNameSource,
	 * source); }
	 */

	private boolean executeClass(String fqClassName, IRuntime runtime, boolean intelMode) throws Exception
	{
		List<String> classpath = new ArrayList<>();

		classpath.add(SANDBOX_CLASS_DIR.toString());

		classpath.addAll(logParser.getConfig().getClassLocations());

		List<String> options = new ArrayList<>();
		options.add("-XX:+UnlockDiagnosticVMOptions");
		options.add("-XX:+TraceClassLoading");
		options.add("-XX:+LogCompilation");
		options.add("-XX:LogFile=" + sandboxLogFile.getCanonicalPath());

		if (logParser.getConfig().isPrintAssembly())
		{
			options.add("-XX:+PrintAssembly");

			if (intelMode)
			{
				options.add("-XX:PrintAssemblyOptions=intel");
			}
		}

		boolean isDisableInlining = logParser.getConfig().isDisableInlining();

		if (isDisableInlining)
		{
			options.add("-XX:-Inline");
		}

		TieredCompilation tieredMode = logParser.getConfig().getTieredCompilationMode();

		if (tieredMode == TieredCompilation.FORCE_TIERED)
		{
			options.add("-XX:+TieredCompilation");
		}
		else if (tieredMode == TieredCompilation.FORCE_NO_TIERED)
		{
			options.add("-XX:-TieredCompilation");
		}

		CompressedOops oopsMode = logParser.getConfig().getCompressedOopsMode();

		if (oopsMode == CompressedOops.FORCE_COMPRESSED)
		{
			options.add("-XX:+UseCompressedOops");
		}
		else if (oopsMode == CompressedOops.FORCE_NO_COMPRESSED)
		{
			options.add("-XX:-UseCompressedOops");
		}

		if (!isDisableInlining && logParser.getConfig().getFreqInlineSize() != JITWatchConstants.DEFAULT_FREQ_INLINE_SIZE)
		{
			options.add("-XX:FreqInlineSize=" + logParser.getConfig().getFreqInlineSize());
		}

		if (!isDisableInlining && logParser.getConfig().getMaxInlineSize() != JITWatchConstants.DEFAULT_MAX_INLINE_SIZE)
		{
			options.add("-XX:MaxInlineSize=" + logParser.getConfig().getMaxInlineSize());
		}

		if (logParser.getConfig().getCompilerThreshold() != JITWatchConstants.DEFAULT_COMPILER_THRESHOLD)
		{
			options.add("-XX:CompilerThreshold=" + logParser.getConfig().getCompilerThreshold());
		}

		logListener.log("Executing: " + fqClassName);
		logListener.log("Classpath: " + StringUtil.listToString(classpath, File.pathSeparatorChar));
		logListener.log("VM options: " + StringUtil.listToString(options));

		return runtime.execute(fqClassName, classpath, options, logListener);
	}

	private void runJITWatch() throws IOException
	{
		JITWatchConfig config = logParser.getConfig();

		List<String> sourceLocations = new ArrayList<>(config.getSourceLocations());
		List<String> classLocations = new ArrayList<>(config.getClassLocations());

		String sandboxSourceDirString = SANDBOX_SOURCE_DIR.toString();
		String sandboxClassDirString = SANDBOX_CLASS_DIR.toString();

		boolean configChanged = false;

		if (!sourceLocations.contains(sandboxSourceDirString))
		{
			configChanged = true;
			sourceLocations.add(sandboxSourceDirString);
		}

		if (!classLocations.contains(sandboxClassDirString))
		{
			configChanged = true;
			classLocations.add(sandboxClassDirString);
		}

		File jdkSrcZip = JITWatchConfig.getJDKSourceZip();

		if (jdkSrcZip != null)
		{
			String jdkSourceZipString = jdkSrcZip.toPath().toString();

			if (!sourceLocations.contains(jdkSourceZipString))
			{
				configChanged = true;
				sourceLocations.add(jdkSourceZipString);
			}
		}

		config.setSourceLocations(sourceLocations);
		config.setClassLocations(classLocations);

		if (configChanged)
		{
			config.saveConfig();
		}

		logParser.reset();

		logParser.processLogFile(sandboxLogFile, sandboxStage);

		logListener.log("Parsing complete");
	}

	private void showTriView(String language, String openClassInTriView)
	{
		IReadOnlyJITDataModel model = logParser.getModel();

		IMetaMember triViewMember = getMemberForClass(openClassInTriView, model);

		if (triViewMember == null && VM_LANGUAGE_SCALA.equals(language) && openClassInTriView.endsWith(S_DOLLAR))
		{
			// Scala and nothing found for Foo$ so try Foo
			triViewMember = getMemberForClass(openClassInTriView.substring(0, openClassInTriView.length() - 1), model);
		}

		sandboxStage.openTriView(triViewMember);
	}

	private IMetaMember getMemberForClass(String openClassInTriView, IReadOnlyJITDataModel model)
	{
		IMetaMember triViewMember = null;

		logListener.log("Looking up class: " + openClassInTriView);

		MetaClass metaClass = model.getPackageManager().getMetaClass(openClassInTriView);

		if (metaClass != null)
		{
			logListener.log("looking for compiled members of " + metaClass.getFullyQualifiedName());

			// select first compiled member if any
			List<IMetaMember> memberList = metaClass.getMetaMembers();

			for (IMetaMember mm : memberList)
			{
				logListener.log("Checking JIT compilation status of " + mm.toString());

				if (triViewMember == null)
				{
					// take the first member encountered
					triViewMember = mm;
				}

				if (mm.isCompiled())
				{
					// override with the first JIT-compiled member
					triViewMember = mm;
					break;
				}
			}
		}

		return triViewMember;
	}
}