/*
 * Copyright (c) 2013-2015 Chris Newland.
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

    List<CodeCacheEvent> getCodeCacheEvents();
    
    Tag getEndOfLogTag();

    String getVmVersionRelease();
    
	IMetaMember findMetaMember(MemberSignatureParts msp);
    
	MetaClass buildAndGetMetaClass(Class<?> clazz);
}