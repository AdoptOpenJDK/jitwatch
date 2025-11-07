package org.adoptopenjdk.jitwatch.test;

import com.chrisnewland.freelogj.Logger;
import com.chrisnewland.freelogj.LoggerFactory;
import org.adoptopenjdk.jitwatch.model.NumberedLine;
import org.adoptopenjdk.jitwatch.model.SplitLog;
import org.adoptopenjdk.jitwatch.model.assembly.*;
import org.adoptopenjdk.jitwatch.parser.ILogParser;
import org.adoptopenjdk.jitwatch.parser.hotspot.HotSpotLogParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.ToolProvider;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class TestAssemblyParserARMOnJitwatchExamples extends AbstractAssemblyTest {
    private final File javaSource;
    public final String javaHome = System.getProperty("java.home") + "/bin/java";
    protected static final Logger logger = LoggerFactory.getLogger(TestAssemblyParserARMOnJitwatchExamples.class);

    public TestAssemblyParserARMOnJitwatchExamples(File javaSource) {
        this.javaSource = javaSource;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getExampleFiles() throws URISyntaxException {
        List<Object[]> files = new ArrayList<>();

        URL examplesURL = TestAssemblyParserARMOnJitwatchExamples.class
                .getClassLoader()
                .getResource("examples/");

        if (examplesURL == null) {
            throw new RuntimeException("Could not find examples directory in resources");
        }

        File examplesDir = new File(examplesURL.toURI());

        if (examplesDir.exists() && examplesDir.isDirectory()) {
            for (File file : Objects.requireNonNull(examplesDir.listFiles())) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    files.add(new Object[]{file});
                }
            }
        }

        return files;
    }

    @Test
    public void testForMissingAssemblyMnemonics() throws IOException, InterruptedException {
        String className = javaSource.getName();

        // Parse Hotspot log from stdout
        ILogParser logParse = new HotSpotLogParser(UnitTestUtil.getNoOpJITListener());
        logParse.processLogFile(getHotspotLogFromJavaSourceFile(), UnitTestUtil.getNoOpParseErrorListener());

        SplitLog log = logParse.getSplitLog();
        List<NumberedLine> assemblyLines = log.getAssemblyLines();
        String hotspotLog = assemblyLines.stream()
                .map(NumberedLine::getLine)
                .collect(Collectors.joining("\n"));

        // Now, we can obtain just the assembly
        IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.ARM_64);
        AssemblyMethod asmMethod = parser.parseAssembly(hotspotLog);

        Set<String> unparsedInstructions = new HashSet<>();

        for (AssemblyBlock assemblyBlock : asmMethod.getBlocks()) {
            for (final AssemblyInstruction instruction : assemblyBlock.getInstructions()) {
                String instructionLine = instruction.toString();

                if (!instruction.toString().matches("^0x[0-9a-fA-F]{16}.*")) continue;

                if (AssemblyReference.lookupDirective(instruction.getMnemonic(), Architecture.ARM_64) == null
                        &&    AssemblyReference.lookupMnemonic(instruction.getMnemonic(), Architecture.ARM_64) == null
                        && AssemblyReference.lookupMnemonicInfo(instruction.toString().split(":")[1].trim().split(";")[0].trim(), Architecture.ARM_64) == null) {
                    unparsedInstructions.add(instruction.toString().replace("^0x[0-9a-fA-F]{16}.*\"", ""));
                }
            }
        }

        if (!unparsedInstructions.isEmpty()) {
            unparsedInstructions.forEach(instruction ->
                    logger.error("Unparsed ARM Mnemonic: {}", instruction)
            );
            fail("Found " + unparsedInstructions.size() + " unparsed mnemonics" + " in " + className);
        }
    }

    private File getHotspotLogFromJavaSourceFile() throws IOException, InterruptedException {
        int exitCode = ToolProvider.getSystemJavaCompiler().run(null, null, null, javaSource.getAbsolutePath());
        if (exitCode != 0) return null;

        String className = javaSource.getName().replace(".java", "");
        String hotspotLogLocation = javaSource.getParent() + "/hotspot-" + className + ".log";

        List<String> hotspotAssemblyCommand = new ArrayList<>();
        hotspotAssemblyCommand.add(javaHome);
        hotspotAssemblyCommand.add("-cp");
        hotspotAssemblyCommand.add(javaSource.getParent());
        hotspotAssemblyCommand.add("-XX:+UnlockDiagnosticVMOptions");
        hotspotAssemblyCommand.add("-XX:+PrintAssembly");
        hotspotAssemblyCommand.add("-XX:+LogCompilation");
        hotspotAssemblyCommand.add("-XX:LogFile="+hotspotLogLocation);
        hotspotAssemblyCommand.add(className);


        ProcessBuilder processBuilder = new ProcessBuilder(hotspotAssemblyCommand);
        processBuilder.directory(javaSource.getParentFile());

        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) { }

        int processExitCode = process.waitFor();
        if (processExitCode != 0) {
            return null;
        }

        Path logPath = Paths.get(hotspotLogLocation);

        return logPath.toFile();
    }
}