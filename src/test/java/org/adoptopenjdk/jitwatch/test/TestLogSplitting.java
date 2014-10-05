package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.adoptopenjdk.jitwatch.core.HotSpotLogParser;
import org.adoptopenjdk.jitwatch.core.ILogParser;
import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.model.SplitLog;
import org.junit.Test;

public class TestLogSplitting
{
	@Test
	public void testCanParseAssemblyAndNMethodMangled() throws IOException
	{
		String nmethodTag = "<nmethod compile_id='1' compiler='C1' level='3' entry='0x00007fb5ad0fe420' size='2504' address='0x00007fb5ad0fe290' relocation_offset='288'/>";
		
		String[] lines = new String[] {
				"Decoding compiled method 0x00007f7d73364190:",
				"Code:",
				"[Disassembling for mach=&apos;i386:x86-64&apos;]",
				"[Entry Point]",
				"[Verified Entry Point]",
				"[Constants]",
				"  # {method} &apos;main&apos; &apos;([Ljava/lang/String;)V&apos; in &apos;org/adoptopenjdk/jitwatch/demo/SandboxTestLoad&apos;",
				"  0x00007f7d733642e0: callq  0x00007f7d77e276f0  ;   {runtime_call}",
				"[Deopt Handler Code]",
				"0x00007fb5ad0fe95c: movabs $0x7fb5ad0fe95c,%r10  ;   {section_word}",
				"0x00007fb5ad0fe966: push   %r10",
				"0x00007fb5ad0fe968: jmpq   0x00007fb5ad047100  ;   {runtime_call}",
				"0x00007fb5ad0fe96d: hlt",
				"0x00007fb5ad0fe96e: hlt",
				"0x00007fb5ad0fe96f: hlt " + nmethodTag,
				"<writer thread='140418643298048'/>" };
		
		StringBuilder builder = new StringBuilder();
		
		for (String line: lines)
		{
			builder.append(line).append(JITWatchConstants.S_NEWLINE);
		}
			
		Path path = Files.createTempFile("testsplit", ".log");
		
		Files.write(path, builder.toString().getBytes(StandardCharsets.UTF_8));
		
		ILogParser parser = new HotSpotLogParser(UnitTestUtil.getNoOpJITListener());
		
		parser.processLogFile(path.toFile(), UnitTestUtil.getNoOpParseErrorListener());
		
		SplitLog log = parser.getSplitLog();
		
		assertEquals(15, log.getAssemblyLines().size());
		assertEquals(2, log.getLogCompilationLines().size());
	}
}