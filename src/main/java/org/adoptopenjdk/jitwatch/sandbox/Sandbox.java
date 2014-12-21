/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.sandbox;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOLLAR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_SCALA;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.ILogParser;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig.CompressedOops;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig.TieredCompilation;
import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.sandbox.compiler.ICompiler;
import org.adoptopenjdk.jitwatch.sandbox.runtime.IRuntime;
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

	private LanguageManager languageManager;

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

		languageManager = new LanguageManager(logParser.getConfig(), logListener);
	}

	public void runSandbox(String language, List<File> compileList, File fileToRun) throws Exception
	{
		logListener.log("Running Sandbox");
		logListener.log("Language is " + language);

		String languagePath = logParser.getConfig().getVMLanguagePath(language);

		logListener.log(language + " home dir: " + languagePath);

		ICompiler compiler = languageManager.getCompiler(language);

		if (compiler == null)
		{
			logListener.log(language + " compiler path not set. Please click Configure Sandbox and set up the path.");
			return;
		}

		IRuntime runtime = languageManager.getRuntime(language);

		if (runtime == null)
		{
			logListener.log(language + " runtime path not set. Please click Configure Sandbox and set up the path.");
			return;
		}

		logListener.log("Compiling: " + StringUtil.listToString(compileList));

		boolean compiledOK = compiler.compile(compileList, buildUniqueClasspath(logParser.getConfig()), SANDBOX_CLASS_DIR.toFile(),
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
				sandboxStage.showError(AbstractProcess.getErrorStream());
			}
		}
		else
		{
			sandboxStage.showError(AbstractProcess.getErrorStream());
		}
	}

	private List<String> buildUniqueClasspath(JITWatchConfig config)
	{
		List<String> classpath = new ArrayList<>();

		classpath.add(SANDBOX_CLASS_DIR.toString());

		for (String path : config.getConfiguredClassLocations())
		{
			if (!classpath.contains(path))
			{
				classpath.add(path);
			}
		}

		return classpath;
	}

	private boolean executeClass(String fqClassName, IRuntime runtime, boolean intelMode) throws Exception
	{
		List<String> classpath = buildUniqueClasspath(logParser.getConfig());

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

		if (logParser.getConfig().getCompileThreshold() != JITWatchConstants.DEFAULT_COMPILER_THRESHOLD)
		{
			options.add("-XX:CompileThreshold=" + logParser.getConfig().getCompileThreshold());
		}

		if (logParser.getConfig().getExtraVMSwitches().length() > 0)
		{
			String extraSwitchString = logParser.getConfig().getExtraVMSwitches();
			String[] switches = extraSwitchString.split(S_SPACE);

			for (String sw : switches)
			{
				options.add(sw);
			}
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
		List<String> classLocations = new ArrayList<>(config.getConfiguredClassLocations());

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