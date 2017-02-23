/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class JITWatchConstants
{
	private JITWatchConstants()
	{
	}

	// Enable debugging for specific functionality
	// DEBUG level logging requires editing src/main/resources/logback.xml
	public static final boolean DEBUG_LOGGING = false;
	public static final boolean DEBUG_LOGGING_BYTECODE = false;
	public static final boolean DEBUG_LOGGING_CLASSPATH = false;
	public static final boolean DEBUG_LOGGING_ASSEMBLY = false;
	public static final boolean DEBUG_LOGGING_SIG_MATCH = false;
	public static final boolean DEBUG_LOGGING_METHOD_ID_MATCH = false;
	public static final boolean DEBUG_LOGGING_OVC = false;
	public static final boolean DEBUG_LOGGING_PARSE_DICTIONARY = false;
	public static final boolean DEBUG_LOGGING_TRIVIEW = false;
	public static final boolean DEBUG_LOGGING_TAGPROCESSOR = false;

	public static final char HEADLESS_SEPARATOR = '|';
	
	public static final boolean DEBUG_MEMBER_CREATION = false;

	public static final int DEFAULT_FREQ_INLINE_SIZE = 35;
	public static final int DEFAULT_MAX_INLINE_SIZE = 325;
	public static final int DEFAULT_COMPILER_THRESHOLD = 10000;

	public static final String TAG_XML = "<?xml";
	public static final String TAG_TTY = "<tty>";
	public static final String TAG_TTY_CLOSE = "</tty>";
	public static final String TAG_COMPILATION_LOG = "<compilation_log";
	public static final String TAG_COMPILATION_LOG_CLOSE = "</compilation_log>";
	public static final String TAG_HOTSPOT_LOG = "<hotspot_log ";
	public static final String TAG_HOTSPOT_LOG_CLOSE = "</hotspot_log>";

	public static final String S_FRAGMENT = "fragment";
	public static final String TAG_OPEN_FRAGMENT = "<fragment>";
	public static final String TAG_CLOSE_FRAGMENT = "</fragment>";
	public static final String TAG_OPEN_CDATA = "<![CDATA[";
	public static final String TAG_CLOSE_CDATA = "]]>";
	public static final String TAG_OPEN_CLOSE_CDATA = TAG_CLOSE_CDATA + TAG_OPEN_CDATA;

	public static final Set<String> SKIP_HEADER_TAGS = new HashSet<>(Arrays.asList(new String[] { TAG_XML, TAG_HOTSPOT_LOG }));

	public static final Set<String> SKIP_BODY_TAGS = new HashSet<>(Arrays.asList(new String[] { TAG_TTY_CLOSE, TAG_COMPILATION_LOG,
			TAG_COMPILATION_LOG_CLOSE, TAG_HOTSPOT_LOG_CLOSE }));

	public static final String NATIVE_CODE_START = "Decoding compiled method";
	public static final String NATIVE_CODE_ENTRY_POINT = "[Entry Point]";
	public static final String NATIVE_CODE_METHOD_MARK = "# {method}";
	public static final String LOADED = "[Loaded ";
	public static final String METHOD = "method";
	public static final String S_PARSE = "parse";
	public static final String S_TYPE = "type";
	public static final String S_PARSE_HIR = "parse_hir";

	public static final String S_CODE_COLON = "Code:";
	public static final String S_ASSEMBLY_CONSTANTS = "[Constants]";

	public static final String DEFAULT_PACKAGE_NAME = "(default package)";
	public static final String TREE_PACKAGE_ROOT = "Packages";

	public static final String TAG_VM_VERSION = "vm_version";
	public static final String TAG_RELEASE = "release";
	public static final String TAG_TWEAK_VM = "TweakVM";

	public static final String TAG_SWEEPER = "sweeper";
	public static final String TAG_CODE_CACHE_FULL = "code_cache_full";
	public static final String TAG_HOTSPOT_LOG_DONE = "hotspot_log_done";

	public static final String TAG_TASK_QUEUED = "task_queued";
	public static final String TAG_NMETHOD = "nmethod";
	public static final String TAG_TASK = "task";
	public static final String TAG_BC = "bc";
	public static final String TAG_CALL = "call";
	public static final String TAG_CODE_CACHE = "code_cache";
	public static final String TAG_TASK_DONE = "task_done";
	public static final String TAG_START_COMPILE_THREAD = "start_compile_thread";
	public static final String TAG_PARSE = S_PARSE;
	public static final String TAG_PHASE = "phase";
	public static final String TAG_KLASS = "klass";
	public static final String TAG_TYPE = S_TYPE;
	public static final String TAG_METHOD = METHOD;
	public static final String TAG_INTRINSIC = "intrinsic";
	public static final String TAG_INLINE_FAIL = "inline_fail";
	public static final String TAG_INLINE_SUCCESS = "inline_success";
	public static final String TAG_BRANCH = "branch";
	public static final String TAG_WRITER = "writer";
	public static final String TAG_VM_ARGUMENTS = "vm_arguments";
	public static final String TAG_ELIMINATE_ALLOCATION = "eliminate_allocation";
	public static final String TAG_ELIMINATE_LOCK = "eliminate_lock";
	public static final String TAG_JVMS = "jvms";
	public static final String TAG_UNCOMMON_TRAP = "uncommon_trap";
	public static final String TAG_PARSE_DONE = "parse_done";
	public static final String TAG_PHASE_DONE = "phase_done";
	public static final String TAG_HOT_THROW = "hot_throw";

	public static final String TAG_CAST_UP = "cast_up";
	public static final String TAG_INLINE_LEVEL_DISCOUNT = "inline_level_discount";
	public static final String TAG_COMMENT= "comment";
	public static final String TAG_PREDICTED_CALL = "predicted_call";
	public static final String TAG_DIRECT_CALL = "direct_call";
	public static final String TAG_VIRTUAL_CALL = "virtual_call";
	public static final String TAG_DEPENDENCY = "dependency";
	public static final String TAG_OBSERVE  = "observe";
	public static final String TAG_ASSERT_NULL  = "assert_null";
	public static final String TAG_FAILURE  = "failure";
	public static final String TAG_LATE_INLINE  = "late_inline";

	public static final String TAG_COMMAND = "command";

	public static final String OSR = "osr";
	public static final String C2N = "c2n";
	public static final String C1 = "C1";
	public static final String C2 = "C2";

	public static final String ATTR_METHOD = METHOD;
	public static final String ATTR_KLASS = "klass";
	public static final String ATTR_COMPILE_ID = "compile_id";
	public static final String ATTR_COMPILE_KIND = "compile_kind";
	public static final String ATTR_STAMP = "stamp";
	public static final String ATTR_STAMP_COMPLETED = "stamp_completed";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_BCI = "bci";
	public static final String ATTR_CODE = "code";
	public static final String ATTR_COMPILER = "compiler";
	public static final String ATTR_LEVEL = "level";
	public static final String ATTR_FREE_CODE_CACHE = "free_code_cache";
	public static final String ATTR_SIZE = "size";
	public static final String ATTR_NMSIZE = "nmsize";
	public static final String ATTR_BYTES = "bytes";
	public static final String ATTR_IICOUNT = "iicount";
	public static final String ATTR_DECOMPILES = "decompiles";
	public static final String ATTR_PARSE = S_PARSE;
	public static final String ATTR_TYPE = S_TYPE;
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
	public static final String ATTR_UNLOADED = "unloaded";
	public static final String ATTR_COUNT = "count";
	public static final String ATTR_PROF_FACTOR = "prof_factor";
	public static final String ATTR_ACTION = "action";
	public static final String ATTR_COMMENT = "comment";
	public static final String ATTR_ADDRESS = "address";
	public static final String ATTR_PREALLOCATED = "preallocated";
	public static final String ATTR_KIND = "kind";

	public static final String BRANCH_TAKEN_ALWAYS = "always";
	public static final String BRANCH_TAKEN_NEVER = "never";
	public static final String BRANCH_TAKEN_MIN = "min";
	public static final String BRANCH_TAKEN_MAX = "max";

	public static final String S_ENTITY_APOS = "&apos;";
	public static final String S_ENTITY_LT = "&lt;";
	public static final String S_ENTITY_GT = "&gt;";

	public static final String S_PACKAGE = "package";
	public static final String S_CLASS = "class";

	public static final String S_PROFILE_DEFAULT = "Default";
	public static final String S_PROFILE_SANDBOX = "Sandbox";

	public static final String S_CLASS_PREFIX_INVOKE = "java.lang.invoke.";
	public static final String S_CLASS_PREFIX_COM_SUN_PROXY = "com.sun.proxy.$Proxy";
	public static final String S_CLASS_PREFIX_STREAM_COLLECTORS = "java.util.stream.Collectors$";
	public static final String S_CLASS_PREFIX_SUN_REFLECT_GENERATED = "sun.reflect.Generated";

	private static final Set<String> SET_AUTOGENERATED_PREFIXES = new HashSet<>();

	static
	{
		SET_AUTOGENERATED_PREFIXES.add(S_CLASS_PREFIX_INVOKE);
		SET_AUTOGENERATED_PREFIXES.add(S_CLASS_PREFIX_STREAM_COLLECTORS);
		SET_AUTOGENERATED_PREFIXES.add(S_CLASS_PREFIX_SUN_REFLECT_GENERATED);
		SET_AUTOGENERATED_PREFIXES.add(S_CLASS_PREFIX_COM_SUN_PROXY);
	}

	public static final Set<String> getAutoGeneratedClassPrefixes()
	{
		return Collections.unmodifiableSet(SET_AUTOGENERATED_PREFIXES);
	}
	
	public static final String S_CLASS_AUTOGENERATED_LAMBDA = "$$Lambda";

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
	public static final String S_CLOSE_ANGLE = ">";
	public static final String S_OPEN_SQUARE_BRACKET = "[";
	public static final String S_CLOSE_SQUARE_BRACKET = "]";
	public static final String S_ARRAY_BRACKET_PAIR = "[]";
	public static final String S_ESCAPED_OPEN_SQUARE = "\\[";
	public static final String S_ESCAPED_CLOSE_SQUARE = "\\]";
	public static final String S_ESCAPED_DOT = "\\.";
	public static final String S_OPEN_BRACE = "{";
	public static final String S_CLOSE_BRACE = "}";
	public static final String S_AT = "@";
	public static final String S_PERCENT = "%";
	public static final String S_DOLLAR = "$";
	public static final String S_HASH = "#";
	public static final String S_SPACE = " ";
	public static final String S_NEWLINE = "\n";
	public static final String S_NEWLINE_CR = "\r";
	public static final String S_TAB = "\t";
	public static final String S_DOUBLE_SPACE = "  ";
	public static final String S_EMPTY = "";
	public static final String S_COLON = ":";
	public static final String S_SEMICOLON = ";";
	public static final String S_VARARGS_DOTS = "...";
	public static final String S_APOSTROPHE = "'";
	public static final String S_OBJECT_ARRAY_DEF = "[L";
	public static final String S_DOT = ".";
	public static final String S_ASTERISK = "*";
	public static final String S_COMMA = ",";
	public static final String S_HYPEN = "-";
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
	public static final String S_BYTECODE_METHOD_COMMENT = "// Method";
	public static final String S_BYTECODE_INTERFACEMETHOD_COMMENT = "// InterfaceMethod";
	public static final String S_BYTECODE_INVOKEDYNAMIC_COMMENT = "// InvokeDynamic";
	public static final String S_DEFAULT = "default";
	public static final String S_FILE_COLON = "file:";
	public static final String S_DOT_CLASS = ".class";
	public static final String S_GENERICS_WILDCARD = "<?>";
	public static final String S_OPTIMIZER = "optimizer";
	public static final String S_SAFEPOINT_POLL = "{poll}";
	public static final String S_SAFEPOINT_POLL_RETURN = "{poll_return}";
	public static final String S_REASON_STALE_TASK="stale task";
		
	public static final String S_TYPE_NAME_SHORT = "short";
	public static final String S_TYPE_NAME_CHARACTER = "char";
	public static final String S_TYPE_NAME_BYTE = "byte";
	public static final String S_TYPE_NAME_LONG = "long";
	public static final String S_TYPE_NAME_DOUBLE = "double";
	public static final String S_TYPE_NAME_BOOLEAN = "boolean";
	public static final String S_TYPE_NAME_INTEGER = "int";
	public static final String S_TYPE_NAME_FLOAT = "float";
	public static final String S_TYPE_NAME_VOID = "void";

	public static final String S_OPTIMIZED_VIRTUAL_CALL = "{optimized virtual_call}";

	public static final char C_SLASH = '/';
	public static final char C_OPEN_ANGLE = '<';
	public static final char C_CLOSE_ANGLE = '>';
	public static final char C_OPEN_PARENTHESES = '(';
	public static final char C_CLOSE_PARENTHESES = ')';
	public static final char C_OPEN_BRACE = '{';
	public static final char C_CLOSE_BRACE = '}';
	public static final char C_SPACE = ' ';
	public static final char C_HASH = '#';
	public static final char C_COMMA = ',';
	public static final char C_AT = '@';
	public static final char C_COLON = ':';
	public static final char C_EQUALS = '=';
	public static final char C_QUOTE = '\'';
	public static final char C_DOUBLE_QUOTE = '"';
	public static final char C_NEWLINE = '\n';
	public static final char C_DOT = '.';
	public static final char C_OBJECT_REF = 'L';
	public static final char C_SEMICOLON = ';';
	public static final char C_OPEN_SQUARE_BRACKET = '[';
	public static final char C_CLOSE_SQUARE_BRACKET = ']';
	public static final char C_QUESTION = '?';
	public static final char C_BACKSLASH = '\\';
	public static final char C_HAT = '^';
	public static final char C_DOLLAR = '$';

	public static final String S_HEX_PREFIX = "0x";
	public static final String S_HEX_POSTFIX = "h";

	public static final String S_BYTECODE_MINOR_VERSION = "minor version:";
	public static final String S_BYTECODE_MAJOR_VERSION = "major version:";
	public static final String S_BYTECODE_SIGNATURE = "Signature:";
	public static final String S_BYTECODE_SOURCE_FILE= "SourceFile:";

	public static final String S_POLYMORPHIC_SIGNATURE = "PolymorphicSignature";

	public static final String S_BYTECODE_CONSTANT_POOL = "Constant pool:";
	public static final String S_BYTECODE_CODE = "Code:";
	public static final String S_BYTECODE_EXCEPTION_TABLE = "Exception table:";
	public static final String S_BYTECODE_RUNTIMEVISIBLEANNOTATIONS = "RuntimeVisibleAnnotations:";
	public static final String S_BYTECODE_LINENUMBERTABLE = "LineNumberTable:";
	public static final String S_BYTECODE_LOCALVARIABLETABLE = "LocalVariableTable:";
	public static final String S_BYTECODE_STACKMAPTABLE = "StackMapTable:";
	public static final String S_BYTECODE_INNERCLASSES = "InnerClasses:";

	public static final String S_CONSTRUCTOR_INIT = "<init>";
	public static final String S_STATIC_INIT = "<clinit>";
	public static final String S_BYTECODE_STATIC_INITIALISER_SIGNATURE = "static {}";

	public static final String PUBLIC = "public";
	public static final String PRIVATE = "private";
	public static final String PROTECTED = "protected";
	public static final String STATIC = "static";
	public static final String FINAL = "final";
	public static final String SYNCHRONIZED = "synchronized";
	public static final String STRICTFP = "strictfp";
	public static final String NATIVE = "native";
	public static final String ABSTRACT = "abstract";

	public static final String[] MODIFIERS = new String[] { PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, SYNCHRONIZED, STRICTFP,
		NATIVE, ABSTRACT };

	public static final Pattern PATTERN_LOG_SIGNATURE = Pattern
			.compile("^([0-9]+):\\s([0-9a-z_]+)\\s?([#0-9a-z,\\- ]+)?\\s?\\{?\\s?(//.*)?");

	public static final String VM_LANGUAGE_JAVA = "Java";
	public static final String VM_LANGUAGE_SCALA = "Scala";
	public static final String VM_LANGUAGE_JRUBY = "JRuby";
	public static final String VM_LANGUAGE_GROOVY = "Groovy";
	public static final String VM_LANGUAGE_KOTLIN = "Kotlin";
	public static final String VM_LANGUAGE_JAVASCRIPT = "JavaScript";
	public static final String VM_LANGUAGE_CLOJURE = "Clojure";

}