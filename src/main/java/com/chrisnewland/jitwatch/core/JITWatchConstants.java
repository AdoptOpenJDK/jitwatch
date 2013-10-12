package com.chrisnewland.jitwatch.core;

public class JITWatchConstants
{
    public static final String TAG_TASK_QUEUED = "<task_queued compile_id";
    public static final String TAG_NMETHOD = "<nmethod";
    public static final String TAG_TASK = "<task compile_id";
    public static final String TAG_TASK_DONE = "<task_done";
    public static final String TAG_START_COMPILE_THREAD = "<start_compile_thread";

    public static final String NATIVE_CODE_METHOD_MARK = "# {method}";

    public static final String LOADED = "[Loaded ";

    public static final String METHOD_START = "method='";

    public static final String OSR = "osr";
    public static final String C2N = "c2n";
    public static final String C1 = "C1";
    public static final String C2= "C2";

    
    public static final String ATTR_COMPILE_ID = "compile_id";
    public static final String ATTR_COMPILE_KIND = "compile_kind";
    public static final String ATTR_STAMP = "stamp";
    public static final String ATTR_COMPILER = "compiler";
    
    public static final String ATTR_NMSIZE =  "nmsize";
    public static final String ATTR_BYTES = "bytes";
    public static final String ATTR_COMPILE_MILLIS =  "compileMillis";
    public static final String ATTR_DECOMPILES = "decompiles";
}