/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;

public class JITWatchConfig
{
	public enum TieredCompilation
	{
		VM_DEFAULT, FORCE_TIERED, FORCE_NO_TIERED;
	}
	
	public enum CompressedOops
	{
		VM_DEFAULT, FORCE_COMPRESSED, FORCE_NO_COMPRESSED;
	}

	private static final Logger logger = LoggerFactory.getLogger(JITWatchConfig.class);

	private static final String PROPERTIES_FILENAME = "jitwatch.properties";

	private static final String KEY_SOURCE_LOCATIONS = "Sources";
	private static final String KEY_CLASS_LOCATIONS = "Classes";

	private static final String KEY_SANDBOX_CLASSPATH = "sandbox.classpath";

	private static final String KEY_SHOW_JIT_ONLY_MEMBERS = "JitOnly";
	private static final String KEY_SHOW_JIT_ONLY_CLASSES = "JitOnlyClasses";
	private static final String KEY_SHOW_HIDE_INTERFACES = "HideInterfaces";
	private static final String KEY_SHOW_NOTHING_MOUNTED = "ShowNothingMounted";
	private static final String KEY_LAST_LOG_DIR = "LastLogDir";

	private static final String KEY_SANDBOX_INTEL_MODE = "sandbox.intel.mode";
	private static final String KEY_SANDBOX_TIERED_MODE = "sandbox.tiered.mode";
	private static final String KEY_SANDBOX_COMPRESSED_OOPS_MODE = "sandbox.compressed.oops.mode";

	private static final String KEY_SANDBOX_FREQ_INLINE_SIZE = "sandbox.freq.inline.size";
	private static final String KEY_SANDBOX_MAX_INLINE_SIZE = "sandbox.max.inline.size";
	private static final String KEY_SANDBOX_PRINT_ASSEMBLY = "sandbox.print.assembly";
	private static final String KEY_SANDBOX_COMPILER_THRESHOLD = "sandbox.compiler.threshold";

	private List<String> sourceLocations = new ArrayList<>();
	private List<String> classLocations = new ArrayList<>();
	private List<String> sandboxClassLocations = new ArrayList<>();

	private boolean showOnlyCompiledMembers = true;
	private boolean showOnlyCompiledClasses = false;
	private boolean hideInterfaces = true;
	private boolean showNothingMounted = true;
	private String lastLogDir = null;
	private boolean intelMode = false;
	private TieredCompilation tieredCompilationMode;
	private CompressedOops compressedOopsMode;

	private int freqInlineSize;
	private int maxInlineSize;
	private boolean printAssembly;
	private int compilerThreshold;

	public JITWatchConfig()
	{
	}

	public JITWatchConfig clone()
	{
		JITWatchConfig copy = new JITWatchConfig();

		Properties saveProperties = getSaveProperties();

		copy.loadFromProperties(saveProperties);

		return copy;
	}

	public void loadFromProperties()
	{
		Properties loadProps = new Properties();

		try (FileReader fr = new FileReader(getConfigFile()))
		{
			loadProps.load(fr);
		}
		catch (FileNotFoundException fnf)
		{
			logger.error("Could not find config file", fnf);
		}
		catch (IOException ioe)
		{
			logger.error("Could not load config file", ioe);
		}

		loadFromProperties(loadProps);
	}

	private void loadFromProperties(Properties loadProps)
	{
		classLocations = loadLocationsFromProperty(loadProps, KEY_CLASS_LOCATIONS);

		sandboxClassLocations = loadLocationsFromProperty(loadProps, KEY_SANDBOX_CLASSPATH);

		sourceLocations = loadLocationsFromProperty(loadProps, KEY_SOURCE_LOCATIONS);

		showOnlyCompiledMembers = loadBooleanFromProperty(loadProps, KEY_SHOW_JIT_ONLY_MEMBERS, true);
		showOnlyCompiledClasses = loadBooleanFromProperty(loadProps, KEY_SHOW_JIT_ONLY_CLASSES, false);
		hideInterfaces = loadBooleanFromProperty(loadProps, KEY_SHOW_HIDE_INTERFACES, true);
		showNothingMounted = loadBooleanFromProperty(loadProps, KEY_SHOW_NOTHING_MOUNTED, true);
		intelMode = loadBooleanFromProperty(loadProps, KEY_SANDBOX_INTEL_MODE, false);

		lastLogDir = loadProps.getProperty(KEY_LAST_LOG_DIR);

		int tieredMode = Integer.parseInt(loadProps.getProperty(KEY_SANDBOX_TIERED_MODE, "0"));

		switch (tieredMode)
		{
		case 0:
			tieredCompilationMode = TieredCompilation.VM_DEFAULT;
			break;
		case 1:
			tieredCompilationMode = TieredCompilation.FORCE_TIERED;
			break;
		case 2:
			tieredCompilationMode = TieredCompilation.FORCE_NO_TIERED;
			break;
		}
		
		int oopsMode = Integer.parseInt(loadProps.getProperty(KEY_SANDBOX_COMPRESSED_OOPS_MODE, "0"));

		switch (oopsMode)
		{
		case 0:
			compressedOopsMode = CompressedOops.VM_DEFAULT;
			break;
		case 1:
			compressedOopsMode = CompressedOops.FORCE_COMPRESSED;
			break;
		case 2:
			compressedOopsMode = CompressedOops.FORCE_NO_COMPRESSED;
			break;
		}
		
		freqInlineSize = loadIntFromProperty(loadProps, KEY_SANDBOX_FREQ_INLINE_SIZE, JITWatchConstants.DEFAULT_FREQ_INLINE_SIZE);
		
		maxInlineSize = loadIntFromProperty(loadProps, KEY_SANDBOX_MAX_INLINE_SIZE, JITWatchConstants.DEFAULT_MAX_INLINE_SIZE);
	
		printAssembly = loadBooleanFromProperty(loadProps, KEY_SANDBOX_PRINT_ASSEMBLY, true);

		compilerThreshold = loadIntFromProperty(loadProps, KEY_SANDBOX_COMPILER_THRESHOLD,  JITWatchConstants.DEFAULT_COMPILER_THRESHOLD);
	}

	private boolean loadBooleanFromProperty(Properties props, String propertyName, boolean defaultValue)
	{
		return Boolean.parseBoolean(props.getProperty(propertyName, new Boolean(defaultValue).toString()));
	}
	
	private int loadIntFromProperty(Properties props, String propertyName, int defaultValue)
	{
		return Integer.parseInt(props.getProperty(propertyName, new Integer(defaultValue).toString()));
	}

	private List<String> loadLocationsFromProperty(Properties props, String propertyName)
	{
		String propValue = props.getProperty(propertyName);

		List<String> result;

		if (propValue != null && propValue.trim().length() > 0)
		{
			result = StringUtil.textToList(propValue, S_COMMA);
		}
		else
		{
			result = new ArrayList<>();
		}

		return result;

	}

	private Properties getSaveProperties()
	{
		Properties saveProps = new Properties();

		saveProps.put(KEY_SOURCE_LOCATIONS, StringUtil.listToText(sourceLocations, S_COMMA));
		saveProps.put(KEY_CLASS_LOCATIONS, StringUtil.listToText(classLocations, S_COMMA));
		saveProps.put(KEY_SANDBOX_CLASSPATH, StringUtil.listToText(sandboxClassLocations, S_COMMA));
		saveProps.put(KEY_SHOW_JIT_ONLY_MEMBERS, Boolean.toString(showOnlyCompiledMembers));
		saveProps.put(KEY_SHOW_JIT_ONLY_CLASSES, Boolean.toString(showOnlyCompiledClasses));
		saveProps.put(KEY_SHOW_HIDE_INTERFACES, Boolean.toString(hideInterfaces));
		saveProps.put(KEY_SHOW_NOTHING_MOUNTED, Boolean.toString(showNothingMounted));
		saveProps.put(KEY_SANDBOX_INTEL_MODE, Boolean.toString(intelMode));

		switch (tieredCompilationMode)
		{
		case VM_DEFAULT:
			saveProps.put(KEY_SANDBOX_TIERED_MODE, "0");
			break;
		case FORCE_TIERED:
			saveProps.put(KEY_SANDBOX_TIERED_MODE, "1");
			break;
		case FORCE_NO_TIERED:
			saveProps.put(KEY_SANDBOX_TIERED_MODE, "2");
			break;
		}
		
		switch (compressedOopsMode)
		{
		case VM_DEFAULT:
			saveProps.put(KEY_SANDBOX_COMPRESSED_OOPS_MODE, "0");
			break;
		case FORCE_COMPRESSED:
			saveProps.put(KEY_SANDBOX_COMPRESSED_OOPS_MODE, "1");
			break;
		case FORCE_NO_COMPRESSED:
			saveProps.put(KEY_SANDBOX_COMPRESSED_OOPS_MODE, "2");
			break;
		}

		if (lastLogDir != null)
		{
			saveProps.put(KEY_LAST_LOG_DIR, lastLogDir);
		}
		
		saveProps.put(KEY_SANDBOX_FREQ_INLINE_SIZE, Integer.toString(freqInlineSize));
	
		saveProps.put(KEY_SANDBOX_MAX_INLINE_SIZE, Integer.toString(maxInlineSize));

		saveProps.put(KEY_SANDBOX_PRINT_ASSEMBLY, Boolean.toString(printAssembly));

		saveProps.put(KEY_SANDBOX_COMPILER_THRESHOLD, Integer.toString(compilerThreshold));

		
		return saveProps;
	}

	public void saveConfig()
	{
		Properties saveProps = getSaveProperties();

		try (FileWriter fw = new FileWriter(getConfigFile()))
		{
			saveProps.store(fw, null);
		}
		catch (IOException ioe)
		{
			logger.error("Could not save config file", ioe);
		}
	}

	private File getConfigFile()
	{
		return new File(System.getProperty("user.dir"), PROPERTIES_FILENAME);
	}

	public static File getJDKSourceZip()
	{
		String jrePath = System.getProperty("java.home");
		File jreDir = new File(jrePath);

		File result = null;

		if (jreDir.exists() && jreDir.isDirectory())
		{
			File parentDir = jreDir.getParentFile();

			if (parentDir.exists() && parentDir.isDirectory())
			{
				File srcZipFile = new File(parentDir, "src.zip");

				if (srcZipFile.exists() && srcZipFile.isFile())
				{
					result = srcZipFile;
				}
			}
		}

		return result;
	}

	public List<String> getClassLocations()
	{
		return Collections.unmodifiableList(classLocations);
	}

	public List<String> getSandboxClassLocations()
	{
		return Collections.unmodifiableList(sandboxClassLocations);
	}

	public List<String> getSourceLocations()
	{
		return Collections.unmodifiableList(sourceLocations);
	}

	public void setSourceLocations(List<String> sourceLocations)
	{
		this.sourceLocations = sourceLocations;
	}

	public void setClassLocations(List<String> classLocations)
	{
		this.classLocations = classLocations;
	}

	public void setSandboxClassLocations(List<String> sandboxClassLocations)
	{
		this.sandboxClassLocations = sandboxClassLocations;
	}

	public boolean isShowOnlyCompiledMembers()
	{
		return showOnlyCompiledMembers;
	}

	public void setShowOnlyCompiledMembers(boolean showOnlyCompiledMembers)
	{
		this.showOnlyCompiledMembers = showOnlyCompiledMembers;
	}

	public boolean isShowOnlyCompiledClasses()
	{
		return showOnlyCompiledClasses;
	}

	public void setShowOnlyCompiledClasses(boolean showOnlyCompiledClasses)
	{
		this.showOnlyCompiledClasses = showOnlyCompiledClasses;
	}

	public boolean isHideInterfaces()
	{
		return hideInterfaces;
	}

	public void setHideInterfaces(boolean hideInterfaces)
	{
		this.hideInterfaces = hideInterfaces;
	}

	public boolean isShowNothingMounted()
	{
		return showNothingMounted;
	}

	public void setShowNothingMounted(boolean showNothingMounted)
	{
		this.showNothingMounted = showNothingMounted;
	}

	public String getLastLogDir()
	{
		return lastLogDir;
	}

	public void setLastLogDir(String lastLogDir)
	{
		this.lastLogDir = lastLogDir;
	}

	public boolean isSandboxIntelMode()
	{
		return intelMode;
	}

	public void setSandboxIntelMode(boolean intelMode)
	{
		this.intelMode = intelMode;
	}

	public TieredCompilation getTieredCompilationMode()
	{
		return tieredCompilationMode;
	}

	public void setTieredCompilationMode(TieredCompilation tieredCompilation)
	{
		this.tieredCompilationMode = tieredCompilation;
	}

	public int getFreqInlineSize()
	{
		return freqInlineSize;
	}

	public void setFreqInlineSize(int freqInlineSize)
	{
		this.freqInlineSize = freqInlineSize;
	}

	public int getMaxInlineSize()
	{
		return maxInlineSize;
	}

	public void setMaxInlineSize(int maxInlineSize)
	{
		this.maxInlineSize = maxInlineSize;
	}

	public boolean isPrintAssembly()
	{
		return printAssembly;
	}

	public void setPrintAssembly(boolean printAssembly)
	{
		this.printAssembly = printAssembly;
	}
	
	public int getCompilerThreshold()
	{
		return compilerThreshold;
	}

	public void setCompilerThreshold(int compilationThreshold)
	{
		this.compilerThreshold = compilationThreshold;
	}
	
	public CompressedOops getCompressedOopsMode()
	{
		return compressedOopsMode;
	}

	public void setCompressedOopsMode(CompressedOops compressedOopsMode)
	{
		this.compressedOopsMode = compressedOopsMode;
	}
}