/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import org.adoptopenjdk.jitwatch.util.StringUtil;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class JITEvent
{
    private long stamp;
    private EventType eventType;
    private String methodSignature;
    private String stampString;

    public JITEvent(long stamp, EventType eventType, String methodSignature)
    {
        this.stamp = stamp;
        this.eventType = eventType;
        this.methodSignature = methodSignature;

        this.stampString = StringUtil.formatTimestamp(stamp, true);
    }

    public long getStamp()
    {
        return stamp;
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public String getMethodSignature()
    {
        return methodSignature;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(stampString).append(C_SPACE);

        sb.append(StringUtil.padLeft(eventType.getText(), 14)).append(C_SPACE).append(C_COLON).append(C_SPACE);
     
        sb.append(methodSignature);

        return sb.toString();
    }
}
