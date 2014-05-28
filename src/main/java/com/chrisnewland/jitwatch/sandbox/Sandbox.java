package com.chrisnewland.jitwatch.sandbox;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_DOT;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.chrisnewland.jitwatch.core.ILogParser;
import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.ui.sandbox.ISandboxStage;
import com.chrisnewland.jitwatch.util.DisassemblyUtil;
import com.chrisnewland.jitwatch.util.FileUtil;
import com.chrisnewland.jitwatch.util.ParseUtil;
import com.chrisnewland.jitwatch.util.StringUtil;

public class Sandbox
{
	private ISandboxStage sandboxStage;
	
	public static final Path SANDBOX_DIR;
	public static final Path SANDBOX_SOURCE_DIR;
	public static final Path SANDBOX_CLASS_DIR;

	private static final String SANDBOX_LOGFILE = "sandbox.log";

	private File sandboxLogFile = new File(SANDBOX_DIR.toFile(), SANDBOX_LOGFILE);

	private ILogParser logParser;

	private String firstClassName;

	private String classContainingMain;
	
	static
	{
		String userDir = System.getProperty("user.dir");

		File sandbox = new File(userDir, "sandbox");
		File sandboxSources = new File(sandbox, "sources");
		File sandboxClasses = new File(sandbox, "classes");

		if (!sandboxSources.exists())
		{
			sandboxSources.mkdirs();
			
			if (sandboxSources.exists())
			{
				copyExamples();
			}
		}

		if (!sandboxClasses.exists())
		{
			sandboxClasses.mkdirs();
		}

		SANDBOX_DIR = sandbox.toPath();
		SANDBOX_SOURCE_DIR = sandboxSources.toPath();
		SANDBOX_CLASS_DIR = sandboxClasses.toPath();
	}
	
	public void reset()
	{
		FileUtil.emptyDir(SANDBOX_SOURCE_DIR.toFile());
		FileUtil.emptyDir(SANDBOX_CLASS_DIR.toFile());
		
		copyExamples();
	}
	
	private static void copyExamples()
	{
		FileUtil.copyFilesToDir(new File("src/main/resources/examples"), SANDBOX_SOURCE_DIR.toFile());
	}

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

		boolean compiledOK = compiler.compile(compileList, SANDBOX_CLASS_DIR.toFile());

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

		sandboxStage.log("Writing source file: " + fqNameSource + ".java");

		return FileUtil.writeSource(SANDBOX_SOURCE_DIR.toFile(), fqNameSource, source);
	}

	private boolean executeTestLoad(ClassExecutor classExecutor, boolean intelMode) throws Exception
	{
		List<String> classpath = new ArrayList<>();

		classpath.add(SANDBOX_CLASS_DIR.toString());

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

		sourceLocations.add(SANDBOX_SOURCE_DIR.toString());
		classLocations.add(SANDBOX_CLASS_DIR.toString());

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