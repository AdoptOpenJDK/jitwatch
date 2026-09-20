package com.chrisnewland.jitwatch.mcp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Builds the test fixture: a real HotSpot compilation log from a program written to
 * provoke known JIT behaviour.
 *
 * The log has to be produced by an actual JVM run, because the point of the tests is to
 * assert against what HotSpot really decided rather than against a recorded file that
 * could drift from the compiler's behaviour. Generating it here rather than committing it
 * means the tests run against whichever JDK the build is using, and that
 * {@code mvn clean test} works from a fresh clone with nothing to set up.
 *
 * The demo program is {@code src/test/resources/JitDemo.java}; each of its methods
 * provokes one behaviour: a tiny method that gets inlined, a large one that does not, an
 * allocation escape analysis removes, a monomorphic and a megamorphic call, an intrinsic,
 * and a branch that deoptimises.
 */
final class Fixture {

	private static final String DEMO_SOURCE = "JitDemo.java";

	private Fixture() {
	}

	/**
	 * The fixture directory, built on first use.
	 * @return the directory holding hotspot.log, src and classes, or null when this JDK
	 * cannot produce one (no compiler available, or the run failed)
	 */
	static synchronized Path build() {
		Path dir = Paths.get("target", "fixture").toAbsolutePath();

		Path log = dir.resolve("hotspot.log");

		if (Files.exists(log)) {
			return dir;
		}

		try {
			Path sourceDir = dir.resolve("src").resolve("demo");
			Path classesDir = dir.resolve("classes");

			Files.createDirectories(sourceDir);
			Files.createDirectories(classesDir);

			Path source = sourceDir.resolve(DEMO_SOURCE);

			Files.write(source, readDemoSource().getBytes(StandardCharsets.UTF_8));

			Path javaHome = Paths.get(System.getProperty("java.home"));
			Path javac = javaHome.resolve("bin").resolve("javac");
			Path java = javaHome.resolve("bin").resolve("java");

			if (!Files.isExecutable(javac)) {
				// A JRE rather than a JDK: nothing to compile with, so the tests will
				// skip
				return null;
			}

			// -g keeps the line number table, which is what lets answers carry source
			// lines
			if (!run(dir, javac.toString(), "-g", "-d", classesDir.toString(), source.toString())) {
				return null;
			}

			boolean ran = run(dir, java.toString(), "-XX:+UnlockDiagnosticVMOptions", "-XX:+LogCompilation",
					"-XX:LogFile=" + log, "-cp", classesDir.toString(), "demo.JitDemo");

			if (!ran || !Files.exists(log)) {
				return null;
			}

			return dir;
		}
		catch (Exception e) {
			return null;
		}
	}

	private static String readDemoSource() throws IOException {
		try (InputStream in = Fixture.class.getResourceAsStream("/" + DEMO_SOURCE)) {
			if (in == null) {
				throw new IOException("Missing test resource " + DEMO_SOURCE);
			}

			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static boolean run(Path workingDir, String... command) throws IOException, InterruptedException {
		List<String> parts = new ArrayList<>(List.of(command));

		Process process = new ProcessBuilder(parts).directory(workingDir.toFile())
			.redirectErrorStream(true)
			.redirectOutput(ProcessBuilder.Redirect.DISCARD)
			.start();

		if (!process.waitFor(120, TimeUnit.SECONDS)) {
			process.destroyForcibly();
			return false;
		}

		return process.exitValue() == 0;
	}

}
