/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

public class CodeCacheEvent
{
    private long stamp;
    private Tag tag;

    public CodeCacheEvent(long stamp, Tag tag)
    {
        this.stamp = stamp;
      	this.tag = tag;
    }

    public long getStamp()
    {
        return stamp;
    }

    public Tag getTag()
    {
        return tag;
    }
}