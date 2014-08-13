/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

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

	private static final String KEY_SHOW_JIT_ONLY_MEMBERS = "JitOnly";
	private static final String KEY_SHOW_JIT_ONLY_CLASSES = "JitOnlyClasses";
	private static final String KEY_SHOW_HIDE_INTERFACES = "HideInterfaces";
	private static final String KEY_SHOW_NOTHING_MOUNTED = "ShowNothingMounted";
	private static final String KEY_LAST_LOG_DIR = "LastLogDir";

	private static final String SANDBOX_PREFIX = "sandbox";
	private static final String KEY_SANDBOX_INTEL_MODE = SANDBOX_PREFIX + ".intel.mode";
	private static final String KEY_SANDBOX_TIERED_MODE = SANDBOX_PREFIX + ".tiered.mode";
	private static final String KEY_SANDBOX_COMPRESSED_OOPS_MODE = SANDBOX_PREFIX + ".compressed.oops.mode";
	private static final String KEY_SANDBOX_FREQ_INLINE_SIZE = SANDBOX_PREFIX + ".freq.inline.size";
	private static final String KEY_SANDBOX_MAX_INLINE_SIZE = SANDBOX_PREFIX + ".max.inline.size";
	private static final String KEY_SANDBOX_PRINT_ASSEMBLY = SANDBOX_PREFIX + ".print.assembly";
	private static final String KEY_SANDBOX_COMPILER_THRESHOLD = SANDBOX_PREFIX + ".compiler.threshold";

	private static final String KEY_LAST_PROFILE = "last.profile";

	private List<String> sourceLocations = new ArrayList<>();
	private List<String> classLocations = new ArrayList<>();

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

	private String profileName = S_PROFILE_DEFAULT;

	private File propertiesFile = new File(System.getProperty("user.dir"), PROPERTIES_FILENAME);

	private Properties loadedProps;
	
	private String preSandboxProfile = S_PROFILE_DEFAULT;

	public JITWatchConfig()
	{
		initialise();
	}

	public JITWatchConfig(File propertiesFile)
	{
		this.propertiesFile = propertiesFile;
		initialise();
	}

	private void initialise()
	{
		logger.debug("initialise: {}", propertiesFile);

		loadedProps = new Properties();
		loadPropertiesFromFile();
	}

	public void switchToSandbox()
	{
		logger.debug("switchToSandbox()");

		preSandboxProfile = profileName;
		setProfileName(S_PROFILE_SANDBOX);
	}
	
	public void switchFromSandbox()
	{
		logger.debug("switchFromSandbox()");
		setProfileName(preSandboxProfile);
	}
	
	public JITWatchConfig clone()
	{
		logger.debug("clone()");

		JITWatchConfig copy = new JITWatchConfig();

		marshalConfigToProperties();

		savePropertiesToFile();

		copy.loadPropertiesFromFile();

		return copy;
	}

	public void setProfileName(String name)
	{
		this.profileName = name;

		logger.debug("setProfileName: {}", name);

		unmarshalPropertiesToConfig();

		marshalConfigToProperties();

		savePropertiesToFile();
	}

	public String getProfileName()
	{
		return profileName;
	}

	public void deleteProfile(String name)
	{
		logger.debug("deleteProfile: {}", name);

		if (name != null && !isBuiltInProfile(name))
		{
			String[] keys = new String[] { KEY_SOURCE_LOCATIONS, KEY_CLASS_LOCATIONS,
					KEY_SHOW_JIT_ONLY_MEMBERS, KEY_SHOW_JIT_ONLY_CLASSES, KEY_SHOW_HIDE_INTERFACES };

			for (String key : keys)
			{
				String deletePropertyKey = key + S_DOT + name;
								
				loadedProps.remove(deletePropertyKey);
			}

			setProfileName(S_PROFILE_DEFAULT);
		}
	}
	
	public boolean isBuiltInProfile(String profileName)
	{
		return S_PROFILE_DEFAULT.equals(profileName) || S_PROFILE_SANDBOX.equals(profileName);
	}

	public Set<String> getProfileNames()
	{
		Set<String> result = new HashSet<>();

		result.add(S_PROFILE_DEFAULT);
		result.add(S_PROFILE_SANDBOX);

		if (profileName != null)
		{
			result.add(profileName);
		}

		for (Object key : loadedProps.keySet())
		{
			String keyString = key.toString();

			if (keyString.startsWith(KEY_SOURCE_LOCATIONS))
			{
				if (keyString.length() > KEY_SOURCE_LOCATIONS.length())
				{
					String profileName = keyString.substring(1 + KEY_SOURCE_LOCATIONS.length());

					result.add(profileName);
				}
			}
		}

		return result;
	}

	private void loadPropertiesFromFile()
	{
		logger.debug("loadPropertiesFromFile({})", propertiesFile);

		try (FileReader fr = new FileReader(propertiesFile))
		{
			loadedProps.load(fr);
		}
		catch (FileNotFoundException fnf)
		{
			logger.warn("Could not find config file {}", propertiesFile.getName());
		}
		catch (IOException ioe)
		{
			logger.error("Could not load config file", ioe);
		}

		profileName = loadedProps.getProperty(KEY_LAST_PROFILE, S_PROFILE_DEFAULT);
		
		if (S_PROFILE_SANDBOX.equals(profileName))
		{
			logger.debug("Resetting last used profile to Default from Sandbox");
			profileName = S_PROFILE_DEFAULT;
		}

		unmarshalPropertiesToConfig();
	}

	public void unmarshalPropertiesToConfig()
	{
		logger.debug("unmarshalPropertiesToConfig({})", profileName);

		classLocations = loadLocationsFromProperty(loadedProps, KEY_CLASS_LOCATIONS);
		sourceLocations = loadLocationsFromProperty(loadedProps, KEY_SOURCE_LOCATIONS);

		showOnlyCompiledMembers = loadBooleanFromProperty(loadedProps, KEY_SHOW_JIT_ONLY_MEMBERS, true);
		showOnlyCompiledClasses = loadBooleanFromProperty(loadedProps, KEY_SHOW_JIT_ONLY_CLASSES, false);
		hideInterfaces = loadBooleanFromProperty(loadedProps, KEY_SHOW_HIDE_INTERFACES, true);
		showNothingMounted = loadBooleanFromProperty(loadedProps, KEY_SHOW_NOTHING_MOUNTED, true);

		lastLogDir = getProperty(loadedProps, KEY_LAST_LOG_DIR);

		intelMode = loadBooleanFromProperty(loadedProps, KEY_SANDBOX_INTEL_MODE, false);

		int tieredMode = Integer.parseInt(getProperty(loadedProps, KEY_SANDBOX_TIERED_MODE, "0"));

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

		int oopsMode = Integer.parseInt(getProperty(loadedProps, KEY_SANDBOX_COMPRESSED_OOPS_MODE, "0"));

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

		freqInlineSize = loadIntFromProperty(loadedProps, KEY_SANDBOX_FREQ_INLINE_SIZE, JITWatchConstants.DEFAULT_FREQ_INLINE_SIZE);

		maxInlineSize = loadIntFromProperty(loadedProps, KEY_SANDBOX_MAX_INLINE_SIZE, JITWatchConstants.DEFAULT_MAX_INLINE_SIZE);

		printAssembly = loadBooleanFromProperty(loadedProps, KEY_SANDBOX_PRINT_ASSEMBLY, true);

		compilerThreshold = loadIntFromProperty(loadedProps, KEY_SANDBOX_COMPILER_THRESHOLD,
				JITWatchConstants.DEFAULT_COMPILER_THRESHOLD);
	}

	private boolean loadBooleanFromProperty(Properties props, String propertyName, boolean defaultValue)
	{
		return Boolean.parseBoolean(getProperty(props, propertyName, Boolean.toString(defaultValue)));
	}

	private int loadIntFromProperty(Properties props, String propertyName, int defaultValue)
	{
		return Integer.parseInt(getProperty(props, propertyName, Integer.toString(defaultValue)));
	}

	private List<String> loadLocationsFromProperty(Properties props, String propertyName)
	{
		String propValue = getProperty(props, propertyName);

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

	private String getProperty(Properties props, String propertyName)
	{
		return getProperty(props, propertyName, S_EMPTY);
	}

	private String getProperty(Properties props, String propertyName, String defaultValue)
	{
		String searchName = getProfilePropertyName(propertyName);

		return props.getProperty(searchName, defaultValue);
	}

	private void putProperty(Properties props, String propertyName, String value)
	{
		String putName = getProfilePropertyName(propertyName);

		props.put(putName, value);
	}
	
	private String getProfilePropertyName(final String propertyName)
	{
		String result = propertyName;

		if (!isSandboxProperty(result))
		{
			if (profileName != null && !profileName.equals(S_PROFILE_DEFAULT) && profileName.length() > 0)
			{
				result = propertyName + S_DOT + profileName;
			}
		}
		
		return result;
	}
	
	private boolean isSandboxProperty(String propertyName)
	{
		return propertyName.startsWith(SANDBOX_PREFIX);
	}

	public void saveConfig()
	{
		logger.debug("saveConfig()");
		marshalConfigToProperties();
		savePropertiesToFile();
	}

	public void marshalConfigToProperties()
	{
		logger.debug("marshalConfigToProperties({})", profileName);

		loadedProps.put(KEY_LAST_PROFILE, profileName);

		putProperty(loadedProps, KEY_SOURCE_LOCATIONS, StringUtil.listToText(sourceLocations, S_COMMA));
		putProperty(loadedProps, KEY_CLASS_LOCATIONS, StringUtil.listToText(classLocations, S_COMMA));
		putProperty(loadedProps, KEY_SHOW_JIT_ONLY_MEMBERS, Boolean.toString(showOnlyCompiledMembers));
		putProperty(loadedProps, KEY_SHOW_JIT_ONLY_CLASSES, Boolean.toString(showOnlyCompiledClasses));
		putProperty(loadedProps, KEY_SHOW_HIDE_INTERFACES, Boolean.toString(hideInterfaces));
		putProperty(loadedProps, KEY_SHOW_NOTHING_MOUNTED, Boolean.toString(showNothingMounted));
		putProperty(loadedProps, KEY_SANDBOX_INTEL_MODE, Boolean.toString(intelMode));

		switch (tieredCompilationMode)
		{
		case VM_DEFAULT:
			putProperty(loadedProps, KEY_SANDBOX_TIERED_MODE, "0");
			break;
		case FORCE_TIERED:
			putProperty(loadedProps, KEY_SANDBOX_TIERED_MODE, "1");
			break;
		case FORCE_NO_TIERED:
			putProperty(loadedProps, KEY_SANDBOX_TIERED_MODE, "2");
			break;
		}

		switch (compressedOopsMode)
		{
		case VM_DEFAULT:
			putProperty(loadedProps, KEY_SANDBOX_COMPRESSED_OOPS_MODE, "0");
			break;
		case FORCE_COMPRESSED:
			putProperty(loadedProps, KEY_SANDBOX_COMPRESSED_OOPS_MODE, "1");
			break;
		case FORCE_NO_COMPRESSED:
			putProperty(loadedProps, KEY_SANDBOX_COMPRESSED_OOPS_MODE, "2");
			break;
		}

		if (lastLogDir != null)
		{
			putProperty(loadedProps, KEY_LAST_LOG_DIR, lastLogDir);
		}

		putProperty(loadedProps, KEY_SANDBOX_FREQ_INLINE_SIZE, Integer.toString(freqInlineSize));

		putProperty(loadedProps, KEY_SANDBOX_MAX_INLINE_SIZE, Integer.toString(maxInlineSize));

		putProperty(loadedProps, KEY_SANDBOX_PRINT_ASSEMBLY, Boolean.toString(printAssembly));

		putProperty(loadedProps, KEY_SANDBOX_COMPILER_THRESHOLD, Integer.toString(compilerThreshold));
	}

	public void savePropertiesToFile()
	{
		logger.debug("savePropertiesToFile({})", propertiesFile);

		try (FileWriter fw = new FileWriter(propertiesFile))
		{
			loadedProps.store(fw, null);
		}
		catch (IOException ioe)
		{
			logger.error("Could not save config file", ioe);
		}
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