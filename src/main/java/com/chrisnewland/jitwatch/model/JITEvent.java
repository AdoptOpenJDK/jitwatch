/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import com.chrisnewland.jitwatch.util.StringUtil;
import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class JITEvent
{
    private static final int FOURTEEN_ZEROS = 14;
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

        sb.append(StringUtil.padLeft(eventType.getText(), FOURTEEN_ZEROS))
                .append(C_SPACE)
                .append(C_COLON)
                .append(C_SPACE);
     
        sb.append(methodSignature);

        return sb.toString();
    }
}
