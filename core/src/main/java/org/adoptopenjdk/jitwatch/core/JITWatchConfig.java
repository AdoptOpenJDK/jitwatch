/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_PROFILE_DEFAULT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_PROFILE_SANDBOX;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.adoptopenjdk.jitwatch.model.ParsedClasspath;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public enum BackgroundCompilation
	{
		VM_DEFAULT, FORCE_BACKGROUND_COMPILATION, FORCE_NO_BACKGROUND_COMPILATION;
	}

	public enum OnStackReplacement
	{
		VM_DEFAULT, FORCE_ON_STACK_REPLACEMENT, FORCE_NO_ON_STACK_REPLACEMENT;
	}

	private static final Logger logger = LoggerFactory.getLogger(JITWatchConfig.class);

	private static final String PROPERTIES_FILENAME = "jitwatch.properties";

	private static final String KEY_SOURCE_LOCATIONS = "Sources";
	private static final String KEY_CLASS_LOCATIONS = "Classes";

	private static final String KEY_VM_LANGUAGE_PATH = "vm.language.path";

	private static final String KEY_SHOW_JIT_ONLY_MEMBERS = "JitOnly";
	private static final String KEY_SHOW_JIT_ONLY_CLASSES = "JitOnlyClasses";
	private static final String KEY_SHOW_HIDE_INTERFACES = "HideInterfaces";
	private static final String KEY_SHOW_NOTHING_MOUNTED = "ShowNothingMounted";
	private static final String KEY_LAST_LOG_DIR = "LastLogDir";
	private static final String KEY_LAST_SANDBOX_EDITOR_PANES = "LastSandboxEditorPanes";

	private static final String KEY_TRIVIEW_TRILINK_MOUSE_FOLLOW = "triview.mouse_follow";
	private static final String KEY_TRIVIEW_LOCAL_ASM_LABELS = "triview.local_asm_labels";

	private static final String SANDBOX_PREFIX = "sandbox";
	private static final String KEY_SANDBOX_INTEL_MODE = SANDBOX_PREFIX + ".intel.mode";
	private static final String KEY_SANDBOX_TIERED_MODE = SANDBOX_PREFIX + ".tiered.mode";
	private static final String KEY_SANDBOX_COMPRESSED_OOPS_MODE = SANDBOX_PREFIX + ".compressed.oops.mode";
	private static final String KEY_SANDBOX_FREQ_INLINE_SIZE = SANDBOX_PREFIX + ".freq.inline.size";
	private static final String KEY_SANDBOX_MAX_INLINE_SIZE = SANDBOX_PREFIX + ".max.inline.size";
	private static final String KEY_SANDBOX_PRINT_ASSEMBLY = SANDBOX_PREFIX + ".print.assembly";
	private static final String KEY_SANDBOX_DISABLE_INLINING = SANDBOX_PREFIX + ".disable.inlining";
	private static final String KEY_SANDBOX_COMPILER_THRESHOLD = SANDBOX_PREFIX + ".compiler.threshold";
	private static final String KEY_SANDBOX_EXTRA_VM_SWITCHES = SANDBOX_PREFIX + ".extra.vm.switches";
	private static final String KEY_SANDBOX_BACKGROUND_COMPILATION = SANDBOX_PREFIX + ".background.compilation";
	private static final String KEY_SANDBOX_ON_STACK_REPLACEMENT = SANDBOX_PREFIX + ".on.stack.replacement";

	private static final String KEY_LAST_PROFILE = "last.profile";

	private List<String> sourceLocations = new ArrayList<>();
	private List<String> classLocations = new ArrayList<>();
	private List<String> editorPanes = new ArrayList<>();

	private boolean showOnlyCompiledMembers = true;
	private boolean showOnlyCompiledClasses = false;
	private boolean hideInterfaces = true;
	private boolean showNothingMounted = true;
	private String lastLogDir = null;
	private boolean intelMode = false;

	private boolean mouseFollow = false;
	private boolean localAsmLabels = false;

	private TieredCompilation tieredCompilationMode;
	private CompressedOops compressedOopsMode;
	private BackgroundCompilation backgroundCompilationMode;
	private OnStackReplacement onStackReplacementMode;

	private int freqInlineSize;
	private int maxInlineSize;
	private boolean printAssembly;
	private boolean disableInlining = false;

	private int compileThreshold;
	private String extraVMSwitches;

	private String profileName = S_PROFILE_DEFAULT;

	private final String CONFIG_OVERRIDE = System.getProperty("jitwatch.config.file", null);

	private File propertiesFile = (CONFIG_OVERRIDE != null) ? new File(CONFIG_OVERRIDE)
			: new File(System.getProperty("user.dir"), PROPERTIES_FILENAME);

	private Properties loadedProps;

	private String preSandboxProfile = S_PROFILE_DEFAULT;

	private ParsedClasspath parsedClasspath = new ParsedClasspath();

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
		if (DEBUG_LOGGING)
		{
			logger.debug("initialise: {}", propertiesFile);
		}

		loadedProps = new Properties();
		loadPropertiesFromFile();
	}

	public void switchToSandbox()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("switchToSandbox()");
		}

		preSandboxProfile = profileName;
		setProfileName(S_PROFILE_SANDBOX);
	}

	public void switchFromSandbox()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("switchFromSandbox()");
		}

		setProfileName(preSandboxProfile);
	}

	public ParsedClasspath getParsedClasspath()
	{
		return parsedClasspath;
	}

	@Override
	public JITWatchConfig clone()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("clone()");
		}

		JITWatchConfig copy = new JITWatchConfig();

		marshalConfigToProperties();

		savePropertiesToFile();

		copy.loadPropertiesFromFile();

		return copy;
	}

	public void setProfileName(String name)
	{
		this.profileName = name;

		if (DEBUG_LOGGING)
		{
			logger.debug("setProfileName: {}", name);
		}

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
		if (DEBUG_LOGGING)
		{
			logger.debug("deleteProfile: {}", name);
		}

		if (name != null && !isBuiltInProfile(name))
		{
			String[] keys = new String[] { KEY_SOURCE_LOCATIONS, KEY_CLASS_LOCATIONS, KEY_SHOW_JIT_ONLY_MEMBERS,
					KEY_SHOW_JIT_ONLY_CLASSES, KEY_SHOW_HIDE_INTERFACES };

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
		if (DEBUG_LOGGING)
		{
			logger.debug("loadPropertiesFromFile({})", propertiesFile);
		}

		try (FileReader fr = new FileReader(propertiesFile))
		{
			loadedProps.load(fr);
		}
		catch (FileNotFoundException fnf)
		{
			if (DEBUG_LOGGING)
			{
				logger.debug("Could not find config file {}", propertiesFile.getName());
			}
		}
		catch (IOException ioe)
		{
			logger.error("Could not load config file", ioe);
		}

		profileName = loadedProps.getProperty(KEY_LAST_PROFILE, S_PROFILE_DEFAULT);

		if (S_PROFILE_SANDBOX.equals(profileName))
		{
			if (DEBUG_LOGGING)
			{
				logger.debug("Resetting last used profile to Default from Sandbox");
			}

			profileName = S_PROFILE_DEFAULT;
		}

		unmarshalPropertiesToConfig();
	}

	public void unmarshalPropertiesToConfig()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("unmarshalPropertiesToConfig({})", profileName);
		}

		classLocations = loadCommaSeparatedListFromProperty(loadedProps, KEY_CLASS_LOCATIONS);
		sourceLocations = loadCommaSeparatedListFromProperty(loadedProps, KEY_SOURCE_LOCATIONS);
		editorPanes = loadCommaSeparatedListFromProperty(loadedProps, KEY_LAST_SANDBOX_EDITOR_PANES);

		showOnlyCompiledMembers = loadBooleanFromProperty(loadedProps, KEY_SHOW_JIT_ONLY_MEMBERS, true);
		showOnlyCompiledClasses = loadBooleanFromProperty(loadedProps, KEY_SHOW_JIT_ONLY_CLASSES, false);
		hideInterfaces = loadBooleanFromProperty(loadedProps, KEY_SHOW_HIDE_INTERFACES, true);
		showNothingMounted = loadBooleanFromProperty(loadedProps, KEY_SHOW_NOTHING_MOUNTED, true);

		lastLogDir = getProperty(loadedProps, KEY_LAST_LOG_DIR);

		intelMode = loadBooleanFromProperty(loadedProps, KEY_SANDBOX_INTEL_MODE, false);

		mouseFollow = loadBooleanFromProperty(loadedProps, KEY_TRIVIEW_TRILINK_MOUSE_FOLLOW, false);
		localAsmLabels = loadBooleanFromProperty(loadedProps, KEY_TRIVIEW_LOCAL_ASM_LABELS, true);

		loadTieredMode();

		loadCompressedOopsMode();

		loadBackgroundCompilationMode();

		loadOnStackReplacementMode();

		freqInlineSize = loadIntFromProperty(loadedProps, KEY_SANDBOX_FREQ_INLINE_SIZE, JITWatchConstants.DEFAULT_FREQ_INLINE_SIZE);

		maxInlineSize = loadIntFromProperty(loadedProps, KEY_SANDBOX_MAX_INLINE_SIZE, JITWatchConstants.DEFAULT_MAX_INLINE_SIZE);

		printAssembly = loadBooleanFromProperty(loadedProps, KEY_SANDBOX_PRINT_ASSEMBLY, true);
		disableInlining = loadBooleanFromProperty(loadedProps, KEY_SANDBOX_DISABLE_INLINING, false);

		compileThreshold = loadIntFromProperty(loadedProps, KEY_SANDBOX_COMPILER_THRESHOLD,
				JITWatchConstants.DEFAULT_COMPILER_THRESHOLD);

		extraVMSwitches = getProperty(loadedProps, KEY_SANDBOX_EXTRA_VM_SWITCHES, JITWatchConstants.S_EMPTY);
	}

	private void loadTieredMode()
	{
		int param = Integer.parseInt(getProperty(loadedProps, KEY_SANDBOX_TIERED_MODE, "0"));

		switch (param)
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
	}

	private void loadCompressedOopsMode()
	{
		int param = Integer.parseInt(getProperty(loadedProps, KEY_SANDBOX_COMPRESSED_OOPS_MODE, "0"));

		switch (param)
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
	}

	private void loadBackgroundCompilationMode()
	{
		int param = Integer.parseInt(getProperty(loadedProps, KEY_SANDBOX_BACKGROUND_COMPILATION, "2"));

		switch (param)
		{
		case 0:
			backgroundCompilationMode = BackgroundCompilation.VM_DEFAULT;
			break;
		case 1:
			backgroundCompilationMode = BackgroundCompilation.FORCE_BACKGROUND_COMPILATION;
			break;
		case 2:
			backgroundCompilationMode = BackgroundCompilation.FORCE_NO_BACKGROUND_COMPILATION;
			break;
		}
	}

	private void loadOnStackReplacementMode()
	{
		int param = Integer.parseInt(getProperty(loadedProps, KEY_SANDBOX_ON_STACK_REPLACEMENT, "0"));

		switch (param)
		{
		case 0:
			onStackReplacementMode = OnStackReplacement.VM_DEFAULT;
			break;
		case 1:
			onStackReplacementMode = OnStackReplacement.FORCE_ON_STACK_REPLACEMENT;
			break;
		case 2:
			onStackReplacementMode = OnStackReplacement.FORCE_NO_ON_STACK_REPLACEMENT;
			break;
		}
	}

	private void saveTieredCompilationMode()
	{
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
	}

	private void saveCompressedOopsMode()
	{
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
	}

	private void saveBackgroundCompilationMode()
	{
		switch (backgroundCompilationMode)
		{
		case VM_DEFAULT:
			putProperty(loadedProps, KEY_SANDBOX_BACKGROUND_COMPILATION, "0");
			break;
		case FORCE_BACKGROUND_COMPILATION:
			putProperty(loadedProps, KEY_SANDBOX_BACKGROUND_COMPILATION, "1");
			break;
		case FORCE_NO_BACKGROUND_COMPILATION:
			putProperty(loadedProps, KEY_SANDBOX_BACKGROUND_COMPILATION, "2");
			break;
		}
	}

	private void saveOnStackReplacementMode()
	{
		switch (onStackReplacementMode)
		{
		case VM_DEFAULT:
			putProperty(loadedProps, KEY_SANDBOX_ON_STACK_REPLACEMENT, "0");
			break;
		case FORCE_ON_STACK_REPLACEMENT:
			putProperty(loadedProps, KEY_SANDBOX_ON_STACK_REPLACEMENT, "1");
			break;
		case FORCE_NO_ON_STACK_REPLACEMENT:
			putProperty(loadedProps, KEY_SANDBOX_ON_STACK_REPLACEMENT, "2");
			break;
		}
	}

	private boolean loadBooleanFromProperty(Properties props, String propertyName, boolean defaultValue)
	{
		return Boolean.parseBoolean(getProperty(props, propertyName, Boolean.toString(defaultValue)));
	}

	private int loadIntFromProperty(Properties props, String propertyName, int defaultValue)
	{
		return Integer.parseInt(getProperty(props, propertyName, Integer.toString(defaultValue)));
	}

	private List<String> loadCommaSeparatedListFromProperty(Properties props, String propertyName)
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
		if (DEBUG_LOGGING)
		{
			logger.debug("saveConfig()");
		}

		marshalConfigToProperties();
		savePropertiesToFile();
	}

	public void marshalConfigToProperties()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("marshalConfigToProperties({})", profileName);
		}

		loadedProps.put(KEY_LAST_PROFILE, profileName);

		putProperty(loadedProps, KEY_SOURCE_LOCATIONS, StringUtil.listToText(sourceLocations, S_COMMA));
		putProperty(loadedProps, KEY_CLASS_LOCATIONS, StringUtil.listToText(classLocations, S_COMMA));
		putProperty(loadedProps, KEY_LAST_SANDBOX_EDITOR_PANES, StringUtil.listToText(editorPanes, S_COMMA));

		putProperty(loadedProps, KEY_SHOW_JIT_ONLY_MEMBERS, Boolean.toString(showOnlyCompiledMembers));
		putProperty(loadedProps, KEY_SHOW_JIT_ONLY_CLASSES, Boolean.toString(showOnlyCompiledClasses));
		putProperty(loadedProps, KEY_SHOW_HIDE_INTERFACES, Boolean.toString(hideInterfaces));
		putProperty(loadedProps, KEY_SHOW_NOTHING_MOUNTED, Boolean.toString(showNothingMounted));
		putProperty(loadedProps, KEY_SANDBOX_INTEL_MODE, Boolean.toString(intelMode));
		putProperty(loadedProps, KEY_TRIVIEW_TRILINK_MOUSE_FOLLOW, Boolean.toString(mouseFollow));
		putProperty(loadedProps, KEY_TRIVIEW_LOCAL_ASM_LABELS, Boolean.toString(localAsmLabels));

		saveTieredCompilationMode();

		saveCompressedOopsMode();

		saveBackgroundCompilationMode();

		saveOnStackReplacementMode();

		if (lastLogDir != null)
		{
			putProperty(loadedProps, KEY_LAST_LOG_DIR, lastLogDir);
		}

		putProperty(loadedProps, KEY_SANDBOX_FREQ_INLINE_SIZE, Integer.toString(freqInlineSize));

		putProperty(loadedProps, KEY_SANDBOX_MAX_INLINE_SIZE, Integer.toString(maxInlineSize));

		putProperty(loadedProps, KEY_SANDBOX_PRINT_ASSEMBLY, Boolean.toString(printAssembly));

		putProperty(loadedProps, KEY_SANDBOX_DISABLE_INLINING, Boolean.toString(disableInlining));

		putProperty(loadedProps, KEY_SANDBOX_COMPILER_THRESHOLD, Integer.toString(compileThreshold));

		putProperty(loadedProps, KEY_SANDBOX_EXTRA_VM_SWITCHES, extraVMSwitches);

	}

	public void savePropertiesToFile()
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("savePropertiesToFile({})", propertiesFile);
		}

		try (FileWriter fw = new FileWriter(propertiesFile))
		{
			loadedProps.store(fw, null);
		}
		catch (IOException ioe)
		{
			logger.error("Could not save config file", ioe);
		}
	}

	public List<String> getConfiguredClassLocations()
	{
		return Collections.unmodifiableList(classLocations);
	}

	public List<String> getAllClassLocations()
	{
		ParsedClasspath parsedClasspath = getParsedClasspath();

		List<String> mergedClassLocations = new ArrayList<>(classLocations);

		for (String parsedLocation : parsedClasspath.getClassLocations())
		{
			if (!mergedClassLocations.contains(parsedLocation))
			{
				mergedClassLocations.add(parsedLocation);
			}
		}

		return mergedClassLocations;
	}

	public List<String> getSourceLocations()
	{
		return Collections.unmodifiableList(sourceLocations);
	}

	public List<String> getLastEditorPaneList()
	{
		return Collections.unmodifiableList(editorPanes);
	}

	public void addSourceFolder(File sourceFolder)
	{
		if (sourceFolder != null)
		{
			String absPath = sourceFolder.getAbsolutePath();

			if (!sourceLocations.contains(absPath))
			{
				sourceLocations.add(absPath);
			}
		}
		else
		{
			logger.warn("Tried to add a null source folder");
		}
	}

	public void setSourceLocations(List<String> sourceLocations)
	{
		this.sourceLocations = sourceLocations;
	}

	public void setClassLocations(List<String> classLocations)
	{
		this.classLocations = classLocations;
	}

	public void setLastEditorPaneList(List<String> editorPanes)
	{
		this.editorPanes = editorPanes;
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

	public boolean isDisableInlining()
	{
		return disableInlining;
	}

	public void setDisableInlining(boolean disableInlining)
	{
		this.disableInlining = disableInlining;
	}

	public int getCompileThreshold()
	{
		return compileThreshold;
	}

	public void setCompileThreshold(int compileThreshold)
	{
		this.compileThreshold = compileThreshold;
	}

	public String getExtraVMSwitches()
	{
		return extraVMSwitches;
	}

	public void setExtraVMSwitches(String extraVMSwitches)
	{
		this.extraVMSwitches = extraVMSwitches;
	}

	public CompressedOops getCompressedOopsMode()
	{
		return compressedOopsMode;
	}

	public void setCompressedOopsMode(CompressedOops compressedOopsMode)
	{
		this.compressedOopsMode = compressedOopsMode;
	}

	public BackgroundCompilation getBackgroundCompilationMode()
	{
		return backgroundCompilationMode;
	}

	public void setBackgroundCompilationMode(BackgroundCompilation backgroundCompilationMode)
	{
		this.backgroundCompilationMode = backgroundCompilationMode;
	}
	
	public OnStackReplacement getOnStackReplacementMode()
	{
		return onStackReplacementMode;
	}

	public void setOnStackReplacementMode(OnStackReplacement onStackReplacementMode)
	{
		this.onStackReplacementMode = onStackReplacementMode;
	}

	public void addOrUpdateVMLanguage(String language, String path)
	{
		String pathKey = KEY_VM_LANGUAGE_PATH + C_DOT + language;

		loadedProps.setProperty(pathKey, path);
	}

	public String getVMLanguagePath(String language)
	{
		String result = S_EMPTY;

		String pathKey = KEY_VM_LANGUAGE_PATH + C_DOT + language;

		if (loadedProps.containsKey(pathKey))
		{
			result = loadedProps.getProperty(pathKey);
		}

		return result;
	}

	public List<String> getVMLanguageList()
	{
		List<String> languageList = new ArrayList<>();

		for (Object key : loadedProps.keySet())
		{
			String keyString = key.toString();

			if (keyString.startsWith(KEY_VM_LANGUAGE_PATH))
			{
				if (keyString.length() > KEY_VM_LANGUAGE_PATH.length())
				{
					String language = keyString.substring(1 + KEY_VM_LANGUAGE_PATH.length());

					languageList.add(language);
				}
			}
		}

		return languageList;
	}

	public boolean isTriViewMouseFollow()
	{
		return mouseFollow;
	}

	public void setTriViewMouseFollow(boolean mouseFollow)
	{
		this.mouseFollow = mouseFollow;
	}

	public boolean isLocalAsmLabels()
	{
		return localAsmLabels;
	}

	public void setLocalAsmLabels(boolean localAsmLabels)
	{
		this.localAsmLabels = localAsmLabels;
	}
}