/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import org.adoptopenjdk.jitwatch.util.StringUtil;

public class AttributeTableRow
{
    private final String type;
    private final String name;
    private final String value;

    public AttributeTableRow(String type, String name, String value)
    {
        this.type = type;
        this.name = name;
        this.value = StringUtil.replaceXMLEntities(value);
    }

    public String getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }
}
