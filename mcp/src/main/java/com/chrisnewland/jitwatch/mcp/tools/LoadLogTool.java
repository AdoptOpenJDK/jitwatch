package com.chrisnewland.jitwatch.mcp.tools;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.chrisnewland.jitwatch.model.Compilation;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.JITStats;
import com.chrisnewland.jitwatch.parser.ParserType;

import com.chrisnewland.jitwatch.mcp.answer.Answer;
import com.chrisnewland.jitwatch.mcp.findings.FindingsEngine;
import com.chrisnewland.jitwatch.mcp.session.Session;

public final class LoadLogTool implements Tool {

	private final ToolContext context;

	public LoadLogTool(ToolContext context) {
		this.context = context;
	}

	@Override
	public String name() {
		return "load_log";
	}

	@Override
	public String title() {
		return "Load a HotSpot compilation log and start a session";
	}

	@Override
	public String description() {
		return """
				Parse a log written with -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation and return a \
				session id used by every other tool, plus a summary of the run and the compiler flags that \
				were in effect. Mount the source and compiled classes to get source line numbers in answers.""";
	}

	@Override
	public Map<String, Object> schema() {
		return Schemas.object()
			.reqStr("log_path", "Path to the HotSpot compilation log file")
			.strArray("source_roots",
					"Directories or source archives holding the Java source. Optional but recommended.")
			.strArray("class_roots", "Directories or jars holding the compiled classes. Optional but recommended.")
			.enumStr("parser", "Which JVM wrote the log. Defaults to hotspot.", false, List.of("hotspot", "j9", "zing"))
			.build();
	}

	@Override
	public Answer call(ToolArguments arguments) throws Exception {
		return load(arguments.string("log_path"), arguments.strings("source_roots"), arguments.strings("class_roots"),
				arguments.string("parser"));
	}

	public Answer load(String logPath, List<String> sourceRoots, List<String> classRoots, String parser)
			throws Exception {
		File logFile = context.resolve(logPath);

		List<String> sources = context.resolveAll(sourceRoots);
		List<String> classes = context.resolveAll(classRoots);

		ParserType parserType = ParserType
			.fromString(parser == null || parser.isBlank() ? "HOTSPOT" : parser.toUpperCase(Locale.ROOT));

		Session session = context.sessions().load(logFile, sources, classes, parserType);

		JITStats stats = session.getModel().getJITStats();

		List<IMetaMember> compiled = FindingsEngine.compiledMembers(session, null);

		int c1 = 0;
		int c2 = 0;
		int osr = 0;
		int failed = 0;
		int compilations = 0;

		for (IMetaMember member : compiled) {
			for (Compilation compilation : member.getCompilations()) {
				compilations++;

				if (compilation.isOSR()) {
					osr++;
				}

				if (compilation.isFailed()) {
					failed++;
				}

				if ("C2".equalsIgnoreCase(compilation.getCompiler())) {
					c2++;
				}
				else if ("C1".equalsIgnoreCase(compilation.getCompiler())) {
					c1++;
				}
			}
		}

		Answer answer = new Answer();

		answer.put("session_id", session.getId());
		answer.put("log", session.getLogPath());
		answer.put("jdk_release", session.getJdkRelease());
		answer.put("jdk_major", session.getThresholds().getJdkMajor());
		answer.put("vm_arguments", context.budget().capString(session.getVmArguments()));

		Map<String, Object> counts = new LinkedHashMap<>();
		counts.put("classes_seen", stats.getCountClass());
		counts.put("members_seen", session.getIndex().size());
		counts.put("members_compiled", compiled.size());
		counts.put("compilations", compilations);
		counts.put("c1", c1);
		counts.put("c2", c2);
		counts.put("osr", osr);
		counts.put("failed", failed);
		counts.put("native_bytes", stats.getNativeBytes());
		answer.put("counts", counts);

		answer.put("flags_in_effect", session.getThresholds().toEvidence());
		answer.put("can_answer", session.getCapabilities().toEvidence());

		String warning = session.getCapabilities().warning();

		if (warning != null) {
			answer.note(warning);
		}

		if (!session.getThresholds().isJdkMajorExact()) {
			answer.note("The JDK version could not be read from the log, so default flag values for JDK 8 were used. "
					+ "Any -XX: overrides on the recorded command line were still applied.");
		}

		if (sources.isEmpty()) {
			answer.note("No source roots were given, so answers will not carry source line numbers.");
		}

		if (classes.isEmpty() && session.getCapabilities().unresolvedClasses() > 0) {
			answer.note("No class roots were given. " + session.getCapabilities().unresolvedClasses()
					+ " compilations name classes that could not be loaded and are missing from this session "
					+ "entirely, not merely missing bytecode detail.");
		}
		else if (classes.isEmpty()) {
			answer.note("No class roots were given, so bytecode-level detail may be unavailable for some methods.");
		}
		else if (session.getCapabilities().hasSignificantUnresolved()) {
			answer.note("The mounted class roots do not cover the whole run: "
					+ session.getCapabilities().unresolvedClasses()
					+ " compilations name classes that could not be loaded. Add the dependency jars.");
		}

		if (!session.getParseErrors().isEmpty()) {
			answer.put("parse_errors",
					context.budget().capList(session.getParseErrors(), 5, "parse_errors", answer.getEvidence()));
			answer.note("The log had parse errors. Some findings may be missing.");
		}

		if (compiled.isEmpty()) {
			answer.plain("This log contains no compilations. Check that the program ran with -XX:+LogCompilation and "
					+ "ran long enough for the JIT to compile anything.");
		}
		else if (warning != null) {
			answer.plain("Loaded " + compilations + " compilations of " + compiled.size() + " methods from a JDK "
					+ session.getThresholds().getJdkMajor() + " run as session " + session.getId() + ", but "
					+ Character.toLowerCase(warning.charAt(0)) + warning.substring(1));
		}
		else {
			answer.plain("Loaded " + compilations + " compilations of " + compiled.size() + " methods from a JDK "
					+ session.getThresholds().getJdkMajor() + " run. Session " + session.getId()
					+ ". Call checkup next to see what is worth looking at.");
		}

		return answer;
	}

}
