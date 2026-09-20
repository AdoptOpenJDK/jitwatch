package com.chrisnewland.jitwatch.mcp.session;

import java.util.List;

import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;

import com.chrisnewland.jitwatch.mcp.threshold.Thresholds;

/** One parsed compilation log, plus everything derived from it once. */
public final class Session {

	private final String id;

	private final String logPath;

	private final IReadOnlyJITDataModel model;

	private final JITWatchConfig config;

	private final Thresholds thresholds;

	private final SignatureIndex index;

	private final String jdkRelease;

	private final String vmArguments;

	private final List<String> parseErrors;

	private final BytecodeCache bytecodeCache;

	private final Capabilities capabilities;

	private final LogScan scan;

	private volatile long lastUsed = System.currentTimeMillis();

	public Session(String id, String logPath, IReadOnlyJITDataModel model, JITWatchConfig config, Thresholds thresholds,
			SignatureIndex index, String jdkRelease, String vmArguments, List<String> parseErrors, LogScan scan,
			Capabilities capabilities) {
		this.id = id;
		this.logPath = logPath;
		this.model = model;
		this.config = config;
		this.thresholds = thresholds;
		this.index = index;
		this.jdkRelease = jdkRelease;
		this.vmArguments = vmArguments;
		this.parseErrors = parseErrors;
		this.bytecodeCache = new BytecodeCache(model, config.getAllClassLocations());
		this.scan = scan;
		this.capabilities = capabilities;
	}

	public Capabilities getCapabilities() {
		return capabilities;
	}

	public LogScan getScan() {
		return scan;
	}

	public String getId() {
		return id;
	}

	public String getLogPath() {
		return logPath;
	}

	public IReadOnlyJITDataModel getModel() {
		return model;
	}

	public JITWatchConfig getConfig() {
		return config;
	}

	public Thresholds getThresholds() {
		return thresholds;
	}

	public SignatureIndex getIndex() {
		return index;
	}

	public String getJdkRelease() {
		return jdkRelease;
	}

	public String getVmArguments() {
		return vmArguments;
	}

	public BytecodeCache getBytecodeCache() {
		return bytecodeCache;
	}

	public List<String> getParseErrors() {
		return parseErrors;
	}

	public long getLastUsed() {
		return lastUsed;
	}

	void touch() {
		lastUsed = System.currentTimeMillis();
	}

}
