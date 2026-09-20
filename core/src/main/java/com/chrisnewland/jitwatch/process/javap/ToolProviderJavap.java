package com.chrisnewland.jitwatch.process.javap;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.chrisnewland.jitwatch.util.StringUtil;

import com.chrisnewland.freelogj.Logger;
import com.chrisnewland.freelogj.LoggerFactory;

/**
 * Runs javap in this process through java.util.spi.ToolProvider.
 *
 * {@link ReflectionJavap} reaches for com.sun.tools.javap.JavapTask, which the module
 * system stopped exporting in JDK 9, so on a modern JDK it always throws and every class
 * falls back to forking javap: about 150ms per class against 2.5ms here. ToolProvider is
 * exported from java.base and needs no --add-exports; it is reached by reflection only
 * because core compiles at Java 8, where the type does not exist.
 */
public final class ToolProviderJavap {

	private static final Logger logger = LoggerFactory.getLogger(ToolProviderJavap.class);

	private static boolean checked = false;

	private static Object javapProvider = null;

	private static Method runMethod = null;

	private ToolProviderJavap() {
	}

	public static synchronized boolean isAvailable() {
		if (!checked) {
			checked = true;
			locate();
		}

		return javapProvider != null;
	}

	private static void locate() {
		try {
			Class<?> toolProvider = Class.forName("java.util.spi.ToolProvider");

			Method findFirst = toolProvider.getMethod("findFirst", String.class);

			Object found = findFirst.invoke(null, "javap");

			if (found instanceof Optional<?>) {
				Optional<?> optional = (Optional<?>) found;

				if (optional.isPresent()) {
					javapProvider = optional.get();
					runMethod = toolProvider.getMethod("run", PrintWriter.class, PrintWriter.class, String[].class);
				}
			}
		}
		catch (ClassNotFoundException cnfe) {
			// Java 8, where ReflectionJavap and the javap process are the working routes.
		}
		catch (Throwable t) {
			logger.info("java.util.spi.ToolProvider is unusable, falling back", t);
		}
	}

	public static String getBytecode(List<String> classLocations, String fqClassName) throws Exception {
		if (!isAvailable()) {
			throw new UnsupportedOperationException("No javap ToolProvider on this runtime");
		}

		List<String> args = new ArrayList<>();
		args.add("-c");
		args.add("-p");
		args.add("-v");

		if (classLocations != null && !classLocations.isEmpty()) {
			args.add("-classpath");
			args.add(StringUtil.listToString(classLocations, File.pathSeparatorChar));
		}

		args.add(fqClassName);

		StringWriter out = new StringWriter();
		StringWriter err = new StringWriter();

		int result;

		try (PrintWriter outWriter = new PrintWriter(out); PrintWriter errWriter = new PrintWriter(err)) {
			result = (Integer) runMethod.invoke(javapProvider, outWriter, errWriter,
					args.toArray(new String[args.size()]));
		}

		if (result != 0) {
			throw new UnsupportedOperationException(
					"javap returned " + result + " for " + fqClassName + ": " + err.toString().trim());
		}

		return out.toString();
	}

}
