/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJITWatchConfig
{

	private String testConfigFilename;

	@Before
	public void setUp() throws IOException
	{
		testConfigFilename = File.createTempFile("test", ".properties").getAbsolutePath();
	}

	@After
	public void tearDown()
	{
		if (testConfigFilename != null)
		{
			File configFile = new File(testConfigFilename);

			if (configFile.exists() && configFile.isFile())
			{
				configFile.delete();
			}
		}
	}

	@Test
	public void testConfigOnlyBuiltInProfiles()
	{
		JITWatchConfig config = new JITWatchConfig(new File(testConfigFilename));

		Set<String> configNames = config.getProfileNames();

		assertEquals(2, configNames.size());

		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_DEFAULT));
		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_SANDBOX));

		List<String> sourcesList = config.getSourceLocations();

		assertEquals(0, sourcesList.size());
	}

	@Test
	public void testEmptyConfigSaveReload()
	{
		JITWatchConfig config = new JITWatchConfig(new File(testConfigFilename));

		String foo = "foo";

		List<String> sourcesList = new ArrayList<String>();
		sourcesList.add(foo);

		config.setSourceLocations(sourcesList);

		config.marshalConfigToProperties();

		config.savePropertiesToFile();

		config = new JITWatchConfig(new File(testConfigFilename));

		List<String> retrievedSourcesList = config.getSourceLocations();

		assertEquals(1, retrievedSourcesList.size());

		assertTrue(retrievedSourcesList.contains(foo));

		Set<String> configNames = config.getProfileNames();

		assertEquals(2, configNames.size());

		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_DEFAULT));
		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_SANDBOX));

		config.setProfileName("MISSING");

		List<String> retrievedSourcesList2 = config.getSourceLocations();

		assertEquals(0, retrievedSourcesList2.size());

		assertFalse(retrievedSourcesList2.contains(foo));
	}

	@Test
	public void testSwitchBetweenDefaultAndUserProfiles()
	{
		JITWatchConfig config = new JITWatchConfig(new File(testConfigFilename));

		String foo = "foo";

		List<String> sourcesListFoo = new ArrayList<String>();
		sourcesListFoo.add(foo);

		config.setSourceLocations(sourcesListFoo);

		config.marshalConfigToProperties();

		config.savePropertiesToFile();

		config = new JITWatchConfig(new File(testConfigFilename));

		List<String> retrievedSourcesList = config.getSourceLocations();

		assertEquals(1, retrievedSourcesList.size());

		assertTrue(retrievedSourcesList.contains(foo));

		Set<String> configNames = config.getProfileNames();

		assertEquals(2, configNames.size());

		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_DEFAULT));
		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_SANDBOX));

		String secondProfileName = "Spaceship";
		
		config.setProfileName(secondProfileName);

		List<String> retrievedSourcesList2 = config.getSourceLocations();

		assertEquals(0, retrievedSourcesList2.size());

		String bar = "bar";

		List<String> sourcesListBar = new ArrayList<String>();
		sourcesListBar.add(bar);
		
		config.setSourceLocations(sourcesListBar);
		
		config.saveConfig();
		
		assertEquals(secondProfileName, config.getProfileName());
		
		configNames = config.getProfileNames();

		assertEquals(3, configNames.size());

		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_DEFAULT));
		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_SANDBOX));
		
		assertTrue(configNames.contains(secondProfileName));

		config.setProfileName(JITWatchConstants.S_PROFILE_DEFAULT);
		
		assertEquals(JITWatchConstants.S_PROFILE_DEFAULT, config.getProfileName());

		retrievedSourcesList = config.getSourceLocations();

		assertEquals(1, retrievedSourcesList.size());

		assertTrue(retrievedSourcesList.contains(foo));
		
		config.setProfileName(secondProfileName);
		
		retrievedSourcesList2 = config.getSourceLocations();

		assertEquals(1, retrievedSourcesList2.size());

		assertTrue(retrievedSourcesList2.contains(bar));
		
	}
	
	@Test
	public void testMakeCustomProfileThenDeleteIt()
	{
		JITWatchConfig config = new JITWatchConfig(new File(testConfigFilename));

		String foo = "foo";

		List<String> sourcesListFoo = new ArrayList<String>();
		sourcesListFoo.add(foo);

		config.setSourceLocations(sourcesListFoo);

		config.marshalConfigToProperties();

		config.savePropertiesToFile();

		config = new JITWatchConfig(new File(testConfigFilename));

		List<String> retrievedSourcesList = config.getSourceLocations();

		assertEquals(1, retrievedSourcesList.size());

		assertTrue(retrievedSourcesList.contains(foo));

		Set<String> configNames = config.getProfileNames();

		assertEquals(2, configNames.size());

		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_DEFAULT));
		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_SANDBOX));

		String customProfileName = "Spaceship";
		
		config.setProfileName(customProfileName);

		List<String> retrievedSourcesList2 = config.getSourceLocations();

		assertEquals(0, retrievedSourcesList2.size());

		String bar = "bar";

		List<String> sourcesListBar = new ArrayList<String>();
		sourcesListBar.add(bar);
		
		config.setSourceLocations(sourcesListBar);
		
		config.saveConfig();
		
		assertEquals(customProfileName, config.getProfileName());
		
		configNames = config.getProfileNames();

		assertEquals(3, configNames.size());

		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_DEFAULT));
		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_SANDBOX));
		
		assertTrue(configNames.contains(customProfileName));
		
		config.deleteProfile(customProfileName);
		
		configNames = config.getProfileNames();

		assertEquals(2, configNames.size());
		
		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_DEFAULT));
		assertTrue(configNames.contains(JITWatchConstants.S_PROFILE_SANDBOX));
		
		assertFalse(configNames.contains(customProfileName));
		
		assertEquals(JITWatchConstants.S_PROFILE_DEFAULT, config.getProfileName());

		retrievedSourcesList = config.getSourceLocations();

		assertEquals(1, retrievedSourcesList.size());

		assertTrue(retrievedSourcesList.contains(foo));		
	}
}