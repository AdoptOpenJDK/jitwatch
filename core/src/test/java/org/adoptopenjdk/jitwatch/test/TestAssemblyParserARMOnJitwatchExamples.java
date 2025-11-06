package org.adoptopenjdk.jitwatch.test;

import com.chrisnewland.freelogj.Logger;
import com.chrisnewland.freelogj.LoggerFactory;
import org.adoptopenjdk.jitwatch.model.assembly.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.ToolProvider;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertNotNull;
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
        String hotspotLog = getHotspotLogFromJavaSourceFile();
        IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.ARM_64);
        AssemblyMethod asmMethod = parser.parseAssembly(hotspotLog);

        List<AssemblyInstruction> unparsedInstructions = new ArrayList<>();

        for (AssemblyBlock assemblyBlock : asmMethod.getBlocks()) {
            for (final AssemblyInstruction instruction : assemblyBlock.getInstructions()) {
                if (AssemblyReference.lookupMnemonic(instruction.getMnemonic(), Architecture.ARM_64) == null) {
                    unparsedInstructions.add(instruction);
                }
            }
        }

        if (!unparsedInstructions.isEmpty()) {
            unparsedInstructions.forEach(instruction ->
                logger.error("Unparsed ARM Mnemonic: {}", instruction)
            );
            fail();
        }
    }

    private String getHotspotLogFromJavaSourceFile() throws IOException, InterruptedException {
        int exitCode = ToolProvider.getSystemJavaCompiler().run(null, null, null, javaSource.getAbsolutePath());
        if (exitCode != 0) return "";

        String className = javaSource.getName().replace(".java", "");

        List<String> hotspotAssemblyCommand = new ArrayList<>();
        hotspotAssemblyCommand.add(javaHome);
        hotspotAssemblyCommand.add("-cp");
        hotspotAssemblyCommand.add(javaSource.getParent());
        hotspotAssemblyCommand.add("-XX:+UnlockDiagnosticVMOptions");
        hotspotAssemblyCommand.add("-XX:+PrintAssembly");
        hotspotAssemblyCommand.add(className);

        ProcessBuilder processBuilder = new ProcessBuilder(hotspotAssemblyCommand);
        processBuilder.directory(javaSource.getParentFile());
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int processExitCode = process.waitFor();
        if (processExitCode != 0) {
            return "";
        }

        return output.toString();
    }
}