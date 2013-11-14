package com.chrisnewland.jitwatch.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JITWatchConstants
{
	public static final String TAG_XML = "<?xml";
	public static final String TAG_TTY = "<tty>";
	public static final String TAG_TTY_CLOSE = "</tty>";
	public static final String TAG_COMPILATION_LOG = "<compilation_log";
	public static final String TAG_COMPILATION_LOG_CLOSE = "</compilation_log>";
	public static final String TAG_HOTSPOT_LOG = "<hotspot_log";
	public static final String TAG_HOTSPOT_LOG_CLOSE = "</hotspot_log>";

	public static final Set<String> SKIP_TAGS = new HashSet<>(Arrays.asList(new String[] { TAG_XML, TAG_TTY, TAG_TTY_CLOSE,
			TAG_COMPILATION_LOG, TAG_COMPILATION_LOG_CLOSE, TAG_HOTSPOT_LOG, TAG_HOTSPOT_LOG_CLOSE }));

	public static final String TAG_TASK_QUEUED = "task_queued";
	public static final String TAG_NMETHOD = "nmethod";
	public static final String TAG_TASK = "task";
	public static final String TAG_CODE_CACHE = "code_cache";
	public static final String TAG_TASK_DONE = "task_done";
	public static final String TAG_START_COMPILE_THREAD = "start_compile_thread";
	public static final String TAG_PHASE = "phase";
	public static final String TAG_KLASS = "klass";
	public static final String TAG_METHOD = "method";
	public static final String TAG_INTRINSIC = "intrinsic";


	public static final String NATIVE_CODE_METHOD_MARK = "# {method}";

	public static final String LOADED = "[Loaded ";

	public static final String METHOD = "method";

	public static final String OSR = "osr";
	public static final String C2N = "c2n";
	public static final String C1 = "C1";
	public static final String C2 = "C2";

	public static final String ATTR_COMPILE_ID = "compile_id";
	public static final String ATTR_COMPILE_KIND = "compile_kind";
	public static final String ATTR_STAMP = "stamp";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_COMPILER = "compiler";
	public static final String ATTR_FREE_CODE_CACHE = "free_code_cache";
	public static final String ATTR_NMSIZE = "nmsize";
	public static final String ATTR_BYTES = "bytes";
	public static final String ATTR_COMPILE_MILLIS = "compileMillis";
	public static final String ATTR_DECOMPILES = "decompiles";
	public static final String ATTR_PARSE = "parse";
	public static final String ATTR_ID = "id";


}