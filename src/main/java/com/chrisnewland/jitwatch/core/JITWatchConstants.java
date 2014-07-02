/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class JITWatchConstants
{
    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private JITWatchConstants() {
    }

	public static final String TAG_XML = "<?xml";
	public static final String TAG_TTY = "<tty>";
	public static final String TAG_TTY_CLOSE = "</tty>";
	public static final String TAG_COMPILATION_LOG = "<compilation_log";
	public static final String TAG_COMPILATION_LOG_CLOSE = "</compilation_log>";
	public static final String TAG_HOTSPOT_LOG = "<hotspot_log";
	public static final String TAG_HOTSPOT_LOG_CLOSE = "</hotspot_log>";

	public static final Set<String> SKIP_HEADER_TAGS = new HashSet<>(Arrays.asList(new String[] { TAG_XML, TAG_HOTSPOT_LOG}));
	
	public static final Set<String> SKIP_BODY_TAGS = new HashSet<>(Arrays.asList(new String[] { TAG_TTY_CLOSE,
			TAG_COMPILATION_LOG, TAG_COMPILATION_LOG_CLOSE, TAG_HOTSPOT_LOG_CLOSE }));

	public static final String NATIVE_CODE_START = "Decoding compiled method";
	public static final String NATIVE_CODE_METHOD_MARK = "# {method}";
	public static final String LOADED = "[Loaded ";
	public static final String METHOD = "method";
	public static final String PARSE = "parse";
	public static final String S_CODE_COLON = "Code:";

	public static final String TAG_VM_VERSION = "vm_version";
	public static final String TAG_RELEASE = "release";

	public static final String TAG_TASK_QUEUED = "task_queued";
	public static final String TAG_NMETHOD = "nmethod";
	public static final String TAG_TASK = "task";
	public static final String TAG_BC = "bc";
	public static final String TAG_CALL = "call";
	public static final String TAG_CODE_CACHE = "code_cache";
	public static final String TAG_TASK_DONE = "task_done";
	public static final String TAG_START_COMPILE_THREAD = "start_compile_thread";
	public static final String TAG_PARSE = PARSE;
	public static final String TAG_PHASE = "phase";
	public static final String TAG_KLASS = "klass";
	public static final String TAG_TYPE = "type";
	public static final String TAG_METHOD = METHOD;
	public static final String TAG_INTRINSIC = "intrinsic";
	public static final String TAG_INLINE_FAIL = "inline_fail";
	public static final String TAG_INLINE_SUCCESS = "inline_success";
	public static final String TAG_BRANCH = "branch";
	public static final String TAG_WRITER = "writer";

	public static final String OSR = "osr";
	public static final String C2N = "c2n";
	public static final String C1 = "C1";
	public static final String C2 = "C2";

	public static final String ATTR_METHOD = METHOD;
	public static final String ATTR_COMPILE_ID = "compile_id";
	public static final String ATTR_COMPILE_KIND = "compile_kind";
	public static final String ATTR_STAMP = "stamp";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_BCI = "bci";
	public static final String ATTR_CODE = "code";
	public static final String ATTR_COMPILER = "compiler";
	public static final String ATTR_FREE_CODE_CACHE = "free_code_cache";
	public static final String ATTR_NMSIZE = "nmsize";
	public static final String ATTR_BYTES = "bytes";
	public static final String ATTR_IICOUNT = "iicount";
	public static final String ATTR_COMPILE_MILLIS = "compileMillis";
	public static final String ATTR_DECOMPILES = "decompiles";
	public static final String ATTR_PARSE = PARSE;
	public static final String ATTR_BUILDIR = "buildIR";
	public static final String ATTR_ID = "id";
	public static final String ATTR_HOLDER = "holder";
	public static final String ATTR_RETURN = "return";
	public static final String ATTR_REASON = "reason";
	public static final String ATTR_ARGUMENTS = "arguments";
	public static final String ATTR_BRANCH_COUNT = "cnt";
	public static final String ATTR_BRANCH_TAKEN = "taken";
	public static final String ATTR_BRANCH_NOT_TAKEN = "not_taken";
	public static final String ATTR_BRANCH_PROB = "prob";
	public static final String ATTR_COUNT = "count";
	public static final String ATTR_PROF_FACTOR = "prof_factor";
	
	public static final String ALWAYS = "always";
	public static final String NEVER = "never";
	
	public static final String ENTITY_APOS = "&apos;";
	public static final String S_ENTITY_LT = "&lt;";
	public static final String S_ENTITY_GT = "&gt;";
	
	public static final String S_PACKAGE = "package";
	public static final String S_CLASS = "class";

	public static final String REGEX_GROUP_ANY = "(.*)";
	public static final String REGEX_ZERO_OR_MORE_SPACES = "( )*";
	public static final String REGEX_ONE_OR_MORE_SPACES = "( )+";
	public static final String REGEX_UNICODE_PARAM_NAME = "([0-9\\p{L}_]+)";
	public static final String REGEX_UNICODE_PACKAGE_NAME = "([0-9\\p{L}_\\.]*)";
	
	public static final String S_OPEN_PARENTHESES = "(";
	public static final String S_CLOSE_PARENTHESES = ")";
	public static final String S_ESCAPED_OPEN_PARENTHESES = "\\(";
	public static final String S_ESCAPED_CLOSE_PARENTHESES = "\\)";
    public static final String S_OPEN_ANGLE = "<";
	public static final String S_CLOSE_ANGLE= ">";
    public static final String S_OPEN_SQUARE = "[";
	public static final String S_CLOSE_SQUARE= "]";
    public static final String S_ESCAPED_OPEN_SQUARE = "\\[";
	public static final String S_ESCAPED_CLOSE_SQUARE= "\\]";
	public static final String S_OPEN_BRACE = "{";
	public static final String S_CLOSE_BRACE= "}";
	public static final String S_AT = "@";
	public static final String S_PERCENT = "%";
	public static final String S_DOLLAR = "$";
	public static final String S_HASH = "#";
	public static final String S_SPACE = " ";
	public static final String S_NEWLINE = "\n";
	public static final String S_TAB = "\t";
	public static final String S_DOUBLE_SPACE = "  ";
	public static final String S_EMPTY = "";
	public static final String S_COLON = ":";
	public static final String S_SEMICOLON = ";";
	public static final String S_DOT = ".";
	public static final String S_COMMA = ",";
	public static final String S_SLASH = "/";
	public static final String S_DOUBLE_SLASH = "//";
	public static final String S_QUOTE = "'";
	public static final String S_DOUBLE_QUOTE = "\"";
	public static final String S_REGEX_WHITESPACE = "\\s+";
	public static final String S_BACKSLASH = "\\";
	public static final String S_XML_COMMENT_START = "<!--";
	public static final String S_XML_COMMENT_END = "-->";
	public static final String S_XML_DOC_START = "<?xml";
	public static final String S_XML_DOCTYPE_START = "<!DOCTYPE";

	public static final char C_SLASH = '/';
	public static final char C_OPEN_ANGLE = '<';
	public static final char C_CLOSE_ANGLE = '>';
	public static final char C_OPEN_PARENTHESES = '(';
	public static final char C_CLOSE_PARENTHESES = ')';
	public static final char C_SPACE = ' ';
	public static final char C_HASH = '#';
	public static final char C_COMMA = ',';
	public static final char C_COLON = ':';
	public static final char C_EQUALS = '=';
	public static final char C_QUOTE = '\'';
	public static final char C_DOUBLE_QUOTE = '"';
	public static final char C_NEWLINE = '\n';
	public static final char C_DOT = '.';
	public static final char C_OBJECT_REF = 'L';
	public static final char C_SEMICOLON = ';';
	public static final char C_OPEN_SQUARE_BRACKET = '[';
	public static final char C_QUESTION = '?';
	public static final char C_BACKSLASH = '\\';
	public static final char C_HAT = '^';
	public static final char C_DOLLAR = '$';
	
	public static final String S_ASSEMBLY_ADDRESS = "0x";
	
	public static final String S_BYTECODE_MINOR_VERSION = "minor version:";
	public static final String S_BYTECODE_MAJOR_VERSION = "major version:";

}