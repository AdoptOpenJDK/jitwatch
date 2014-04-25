/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.util.StringUtil;

public class AttributeTableRow
{
    private final String type;
    private final String name;
    private final String value;

    public AttributeTableRow(String type, String name, String value)
    {
        this.type = type;
        this.name = name;
        this.value = StringUtil.formatThousands(value);
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
