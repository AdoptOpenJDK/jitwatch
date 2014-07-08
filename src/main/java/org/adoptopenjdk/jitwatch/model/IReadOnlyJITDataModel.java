/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.util.List;

public interface IReadOnlyJITDataModel
{
    PackageManager getPackageManager();

    JITStats getJITStats();

    List<JITEvent> getEventListCopy();

    List<Tag> getCodeCacheTags();

    String getVmVersionRelease();
    
    // AARRGGG mutator on a read only interface
    // TODO: Fix the design!
	void buildMetaClass(String fqClassName, Class<?> clazz);
}