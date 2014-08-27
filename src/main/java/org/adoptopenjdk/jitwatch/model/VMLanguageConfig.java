package org.adoptopenjdk.jitwatch.model;

import java.util.Properties;

public class VMLanguageConfig
{
	private String language;
	private String compilerPath;
	private String executorPath;
	
	public static final String KEY_COMPILER_PATH = "vm.language.compiler.";
	public static final String KEY_EXECUTOR_PATH = "vm.language.executor.";

	
	
	public VMLanguageConfig(String language, Properties props)
	{
		this.language = language;
	}
	
	public static VMLanguageConfig loadVMLanguageConfig()
	{
		return null;
	}
	
	public Properties exportAsProperties()
	{
		Properties properties = new Properties();
		
		properties.setProperty(KEY_COMPILER_PATH+language, compilerPath);
		properties.setProperty(KEY_EXECUTOR_PATH+language, executorPath);

		return properties;
	}
	
	public String getLanguage()
	{
		return language;
	}

	public String getCompilerPath()
	{
		return compilerPath;
	}

	public void setCompilerPath(String compilerPath)
	{
		this.compilerPath = compilerPath;
	}

	public String getExecutorPath()
	{
		return executorPath;
	}

	public void setExecutorPath(String executorPath)
	{
		this.executorPath = executorPath;
	}
}