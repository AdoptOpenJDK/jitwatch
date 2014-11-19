/*
 * Copyright (c) 2013, 2014 Chris Newland.
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

	// *verbose* debugging
	// DEBUG level logging requires editing src/main/resources/logback.xml
	public static final boolean DEBUG_LOGGING = false;
	public static final boolean DEBUG_LOGGING_BYTECODE = false;
	public static final boolean DEBUG_LOGGING_ASSEMBLY = false;
	public static final boolean DEBUG_LOGGING_SIG_MATCH = true;

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

	public static final Set<String> SKIP_HEADER_TAGS = new HashSet<>(Arrays.asList(new String[] { TAG_XML, TAG_HOTSPOT_LOG }));

	public static final Set<String> SKIP_BODY_TAGS = new HashSet<>(Arrays.asList(new String[] { TAG_TTY_CLOSE, TAG_COMPILATION_LOG,
			TAG_COMPILATION_LOG_CLOSE, TAG_HOTSPOT_LOG_CLOSE }));

	public static final String NATIVE_CODE_START = "Decoding compiled method";
	public static final String NATIVE_CODE_METHOD_MARK = "# {method}";
	public static final String LOADED = "[Loaded ";
	public static final String METHOD = "method";
	public static final String PARSE = "parse";
	public static final String S_CODE_COLON = "Code:";

	public static final String DEFAULT_PACKAGE_NAME = "(default package)";
	public static final String TREE_PACKAGE_ROOT = "Packages";

	public static final String TAG_VM_VERSION = "vm_version";
	public static final String TAG_RELEASE = "release";
	public static final String TAG_TWEAK_VM = "TweakVM";

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
	public static final String TAG_VM_ARGUMENTS = "vm_arguments";
	public static final String TAG_ELIMINATE_ALLOCATION = "eliminate_allocation";
	public static final String TAG_JVMS = "jvms";

	public static final String TAG_COMMAND = "command";

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

	public static final String S_ENTITY_APOS = "&apos;";
	public static final String S_ENTITY_LT = "&lt;";
	public static final String S_ENTITY_GT = "&gt;";

	public static final String S_PACKAGE = "package";
	public static final String S_CLASS = "class";

	public static final String S_PROFILE_DEFAULT = "Default";
	public static final String S_PROFILE_SANDBOX = "Sandbox";

	public static final String S_CLASS_PREFIX_INVOKE = "java.lang.invoke.";
	public static final String S_CLASS_PREFIX_LAMBDAFORM = "java.lang.invoke.LambdaForm";
	public static final String S_CLASS_PREFIX_STREAM_COLLECTORS = "java.util.stream.Collectors$";
	public static final String S_CLASS_PREFIX_LAMBDA_LAMBDA= "Lambda$$Lambda";

	private static final Set<String> SET_LAMBDA_PREFIXES = new HashSet<>();

	static
	{
		SET_LAMBDA_PREFIXES.add(S_CLASS_PREFIX_INVOKE);
		//SET_LAMBDA_PREFIXES.add(S_CLASS_PREFIX_LAMBDAFORM);
		SET_LAMBDA_PREFIXES.add(S_CLASS_PREFIX_STREAM_COLLECTORS);
		SET_LAMBDA_PREFIXES.add(S_CLASS_PREFIX_LAMBDA_LAMBDA);
	}

	public static final Set<String> getLambdaClassPrefixes()
	{
		return Collections.unmodifiableSet(SET_LAMBDA_PREFIXES);
	}

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
	public static final String S_OPEN_SQUARE = "[";
	public static final String S_CLOSE_SQUARE = "]";
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
	public static final String S_TAB = "\t";
	public static final String S_DOUBLE_SPACE = "  ";
	public static final String S_EMPTY = "";
	public static final String S_COLON = ":";
	public static final String S_SEMICOLON = ";";
	public static final String S_VARARGS_DOTS = "...";
	public static final String S_OBJECT_ARRAY_DEF = "[L";
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
	public static final String S_BYTECODE_METHOD_COMMENT = "// Method";
	public static final String S_BYTECODE_INTERFACEMETHOD_COMMENT = "// InterfaceMethod";
	public static final String S_DEFAULT = "default";
	public static final String S_FILE_COLON = "file:";
	public static final String S_DOT_CLASS = ".class";

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
	public static final char C_QUESTION = '?';
	public static final char C_BACKSLASH = '\\';
	public static final char C_HAT = '^';
	public static final char C_DOLLAR = '$';

	public static final String S_ASSEMBLY_ADDRESS = "0x";

	public static final String S_BYTECODE_MINOR_VERSION = "minor version:";
	public static final String S_BYTECODE_MAJOR_VERSION = "major version:";

	public static final String S_POLYMORPHIC_SIGNATURE = "PolymorphicSignature";

	public static final String S_BYTECODE_CONSTANT_POOL = "Constant pool:";
	public static final String S_BYTECODE_CODE = "Code:";
	public static final String S_BYTECODE_EXCEPTIONS = "Exceptions:";
	public static final String S_BYTECODE_RUNTIMEVISIBLEANNOTATIONS = "RuntimeVisibleAnnotations:";
	public static final String S_BYTECODE_LINENUMBERTABLE = "LineNumberTable:";
	public static final String S_BYTECODE_LOCALVARIABLETABLE = "LocalVariableTable:";
	public static final String S_BYTECODE_STACKMAPTABLE = "StackMapTable:";

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
}
