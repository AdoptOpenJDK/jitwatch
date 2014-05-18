package com.chrisnewland.jitwatch.sandbox;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_DOT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.chrisnewland.jitwatch.core.ILogParser;
import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.ui.sandbox.ISandboxStage;
import com.chrisnewland.jitwatch.util.DisassemblyUtil;
import com.chrisnewland.jitwatch.util.ParseUtil;
import com.chrisnewland.jitwatch.util.StringUtil;

public class Sandbox
{
	private ISandboxStage sandboxStage;

	private static final String SANDBOX_LOGFILE = "sandbox.log";

	private File sandboxLogFile = new File(ClassCompiler.SANDBOX_DIR.toFile(), SANDBOX_LOGFILE);

	private ILogParser logParser;

	private String firstClassName;

	private String classContainingMain;

	public Sandbox(ILogParser parser, ISandboxStage logger)
	{
		this.logParser = parser;
		this.sandboxStage = logger;
	}

	public void runSandbox(List<String> sourceFiles, boolean intelMode) throws Exception
	{
		firstClassName = null;
		classContainingMain = null;

		List<File> compileList = new ArrayList<>();

		for (String source : sourceFiles)
		{
			File sourceFile = writeSourceFile(source);
			compileList.add(sourceFile);
		}

		sandboxStage.log("Compiling: " + StringUtil.listToString(compileList));

		ClassCompiler compiler = new ClassCompiler();

		boolean compiledOK = compiler.compile(compileList);

		sandboxStage.log("Compilation success: " + compiledOK);

		if (compiledOK)
		{
			if (classContainingMain != null)
			{
				ClassExecutor classExecutor = new ClassExecutor();

				boolean executionSuccess = executeTestLoad(classExecutor, intelMode);

				sandboxStage.log("Execution success: " + executionSuccess);

				if (executionSuccess)
				{
					runJITWatch();
					showTriView();
				}
				else
				{
					sandboxStage.showError(classExecutor.getErrorStream());
				}
			}
			else
			{
				sandboxStage.log("No main method found");
			}
		}
		else
		{
			String compilationMessages = compiler.getCompilationMessages();
			sandboxStage.showError(compilationMessages);
		}
	}

	private File writeSourceFile(String source) throws IOException
	{
		String sourcePackage = ParseUtil.getPackageFromSource(source);

		String sourceClass = ParseUtil.getClassFromSource(source);

		StringBuilder fqNameSourceBuilder = new StringBuilder();

		if (sourcePackage.length() > 0)
		{
			fqNameSourceBuilder.append(sourcePackage).append(S_DOT);
		}

		fqNameSourceBuilder.append(sourceClass);

		String fqNameSource = fqNameSourceBuilder.toString();

		if (source.contains("public static void main("))
		{
			classContainingMain = fqNameSource;
			sandboxStage.log("Found main method in " + classContainingMain);
		}

		if (firstClassName == null)
		{
			firstClassName = fqNameSource;
		}

		sandboxStage.log("Writing source file: " + fqNameSource);

		return ClassCompiler.writeSource(fqNameSource, source);
	}

	private boolean executeTestLoad(ClassExecutor classExecutor, boolean intelMode) throws Exception
	{
		List<String> classpath = new ArrayList<>();

		classpath.add(ClassCompiler.SANDBOX_CLASS_DIR.toString());

		List<String> options = new ArrayList<>();
		options.add("-XX:+UnlockDiagnosticVMOptions");
		options.add("-XX:+TraceClassLoading");
		options.add("-XX:+LogCompilation");
		options.add("-XX:LogFile=" + sandboxLogFile.getCanonicalPath());

		if (DisassemblyUtil.isDisassemblerAvailable())
		{
			options.add("-XX:+PrintAssembly");

			if (intelMode)
			{
				options.add("-XX:PrintAssemblyOptions=intel");
			}
		}

		sandboxStage.log("Executing: " + classContainingMain);
		sandboxStage.log("VM options: " + StringUtil.listToString(options));

		return classExecutor.execute(classContainingMain, classpath, options);
	}

	private void runJITWatch() throws IOException
	{
		List<String> sourceLocations = new ArrayList<>();
		List<String> classLocations = new ArrayList<>();

		sourceLocations.add(ClassCompiler.SANDBOX_SOURCE_DIR.toString());
		classLocations.add(ClassCompiler.SANDBOX_CLASS_DIR.toString());

		JITWatchConfig config = new JITWatchConfig();
		config.setSourceLocations(sourceLocations);
		config.setClassLocations(classLocations);

		logParser.reset();

		logParser.setConfig(config);

		logParser.readLogFile(sandboxLogFile);

		sandboxStage.log("Parsing complete");
	}

	private void showTriView()
	{
		IReadOnlyJITDataModel model = logParser.getModel();

		sandboxStage.log("Looking up class: " + firstClassName);

		MetaClass metaClass = model.getPackageManager().getMetaClass(firstClassName);

		IMetaMember firstCompiled = null;

		if (metaClass != null)
		{
			sandboxStage.log("Found: " + metaClass.getFullyQualifiedName());

			sandboxStage.log("looking for compiled members");

			// select first compiled member if any
			List<IMetaMember> memberList = metaClass.getMetaMembers();

			for (IMetaMember mm : memberList)
			{
				if (mm.isCompiled())
				{
					firstCompiled = mm;
					break;
				}
			}
		}

		sandboxStage.openTriView(firstCompiled);
	}
}