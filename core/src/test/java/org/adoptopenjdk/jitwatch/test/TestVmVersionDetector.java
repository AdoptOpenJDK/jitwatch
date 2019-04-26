/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import org.adoptopenjdk.jitwatch.core.TagProcessor;
import org.adoptopenjdk.jitwatch.histo.Histo;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.util.VmVersionDetector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestVmVersionDetector
{
	private static final String[] LINES_JDK7_80 = new String[] { "<vm_version>", "<name>", "Java HotSpot(TM) 64-Bit Server VM",
			"</name>", "<release>", "24.80-b11", "</release>", "<info>",
			"Java HotSpot(TM) 64-Bit Server VM (24.80-b11) for linux-amd64 JRE (1.7.0_80-b15), built on Apr 10 2015 19:53:14 by &quot;java_re&quot; with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)",
			"</info>", "</vm_version>" };

	private static final String[] LINES_JDK8_172 = new String[] { "<vm_version>", "<name>", "Java HotSpot(TM) 64-Bit Server VM",
			"</name>", "<release>", "25.172-b11", "</release>", "<info>",
			"Java HotSpot(TM) 64-Bit Server VM (25.172-b11) for linux-amd64 JRE (1.8.0_172-b11), built on Mar 28 2018 21:44:09 by &quot;java_re&quot; with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)",
			"</info>", "</vm_version>" };

	private static final String[] LINES_JDK8_202 = new String[] { "<vm_version>", "<name>", "Java HotSpot(TM) 64-Bit Server VM",
			"</name>", "<release>", "25.202-b08", "</release>", "<info>",
			"Java HotSpot(TM) 64-Bit Server VM (25.202-b08) for linux-amd64 JRE (1.8.0_202-b08), built on Dec 15 2018 12:40:22 by &quot;java_re&quot; with gcc 7.3.0",
			"</info>", "</vm_version>" };

	private static final String[] LINES_JDK9 = new String[] { "<vm_version>", "<name>", "OpenJDK 64-Bit Server VM", "</name>",
			"<release>", "9-internal+0-adhoc.chris.jdk9-dev", "</release>", "<info>",
			"OpenJDK 64-Bit Server VM (9-internal+0-adhoc.chris.jdk9-dev) for linux-amd64 JRE (9-internal+0-adhoc.chris.jdk9-dev), built on Mar 18 2019 09:17:23 by &quot;chris&quot; with gcc 6.3.0 20170516",
			"</info>", "</vm_version>" };

	private static final String[] LINES_JDK10 = new String[] { "<vm_version>", "<name>", "OpenJDK 64-Bit Server VM", "</name>",
			"<release>", "10-internal+0-adhoc.chris.jdk10", "</release>", "<info>",
			"OpenJDK 64-Bit Server VM (10-internal+0-adhoc.chris.jdk10) for linux-amd64 JRE (10-internal+0-adhoc.chris.jdk10), built on Mar 18 2019 09:21:55 by &quot;chris&quot; with gcc 6.3.0 20170516",
			"</info>", "</vm_version>" };

	private static final String[] LINES_JDK11 = new String[] { "<vm_version>", "<name>", "OpenJDK 64-Bit Server VM", "</name>",
			"<release>", "11-internal+0-adhoc.chris.jdk11", "</release>", "<info>",
			"OpenJDK 64-Bit Server VM (11-internal+0-adhoc.chris.jdk11) for linux-amd64 JRE (11-internal+0-adhoc.chris.jdk11), built on Mar 18 2019 09:28:17 by &quot;chris&quot; with gcc 6.3.0 20170516",
			"</info>", "</vm_version>" };

	private static final String[] LINES_JDK12 = new String[] { "<vm_version>", "<name>", "OpenJDK 64-Bit Server VM", "</name>",
			"<release>", "12-internal+0-adhoc.chris.jdk12", "</release>", "<info>",
			"OpenJDK 64-Bit Server VM (12-internal+0-adhoc.chris.jdk12) for linux-amd64 JRE (12-internal+0-adhoc.chris.jdk12), built on Mar 18 2019 09:33:59 by &quot;chris&quot; with gcc 6.3.0 20170516",
			"</info>", "</vm_version>" };

	private static final String[] LINES_JDK13 = new String[] { "<vm_version>", "<name>", "OpenJDK 64-Bit Server VM", "</name>",
			"<release>", "13-internal+0-adhoc.chris.jdk13", "</release>", "<info>",
			"OpenJDK 64-Bit Server VM (13-internal+0-adhoc.chris.jdk13) for linux-amd64 JRE (13-internal+0-adhoc.chris.jdk13), built on Mar 18 2019 09:41:03 by &quot;chris&quot; with gcc 6.3.0 20170516",
			"</info>", "</vm_version>" };

	private static final String[] LINES_ADOPT_JDK11 = new String[] { "<vm_version>", "<name>", "OpenJDK 64-Bit Server VM",
			"</name>", "<release>", "11.0.3+7", "</release>", "<info>",
			"OpenJDK 64-Bit Server VM (11.0.3+7) for linux-amd64 JRE (11.0.3+7), built on Apr 18 2019 11:34:01 by &quot;jenkins&quot; with gcc 7.3.1 20180303 (Red Hat 7.3.1-5)",
			"</info>", "</vm_version>" };

	private static final String[] LINES_ORACLE_JDK11 = new String[] { "<vm_version>", "<name>", "OpenJDK 64-Bit Server VM",
			"</name>", "<release>", "11+28", "</release>", "<info>",
			"OpenJDK 64-Bit Server VM (11+28) for linux-amd64 JRE (11+28), built on Aug 22 2018 18:55:06 by &quot;mach5one&quot; with gcc 7.3.0",
			"</info>", "</vm_version>" };

	private static final String[] LINES_ZULU_JDK11 = new String[] { "<vm_version>", "<name>", "OpenJDK 64-Bit Server VM", "</name>",
			"<release>", "11.0.3+7-LTS", "</release>", "<info>",
			"OpenJDK 64-Bit Server VM (11.0.3+7-LTS) for linux-amd64 JRE (Zulu11.31+11-CA) (11.0.3+7-LTS), built on Apr  6 2019 01:12:35 by &quot;zulu_re&quot; with gcc 4.9.2 20150212 (Red Hat 4.9.2-6)",
			"</info>", "</vm_version>" };

	@Test public void testVMDetectionHotSpot()
	{
		assertEquals(7, testHotSpot(LINES_JDK7_80));

		assertEquals(8, testHotSpot(LINES_JDK8_172));

		assertEquals(8, testHotSpot(LINES_JDK8_202));

		assertEquals(9, testHotSpot(LINES_JDK9));

		assertEquals(10, testHotSpot(LINES_JDK10));

		assertEquals(11, testHotSpot(LINES_JDK11));

		assertEquals(12, testHotSpot(LINES_JDK12));

		assertEquals(13, testHotSpot(LINES_JDK13));

		assertEquals(11, testHotSpot(LINES_ADOPT_JDK11));

		assertEquals(11, testHotSpot(LINES_ZULU_JDK11));

		assertEquals(11, testHotSpot(LINES_ORACLE_JDK11));
	}

	private int testHotSpot(String[] lines)
	{
		TagProcessor tagProcessor = new TagProcessor();

		Tag tag = null;

		for (String line : lines)
		{
			tag = tagProcessor.processLine(line);
		}

		return VmVersionDetector.getMajorVersionFromHotSpotTag(tag);
	}
}