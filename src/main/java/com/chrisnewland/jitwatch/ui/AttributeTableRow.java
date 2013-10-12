/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.text.DecimalFormat;

public class AttributeTableRow
{
    private final String type;
    private final String name;
    private final String value;

    private static final DecimalFormat DF = new DecimalFormat("#,###");

    public AttributeTableRow(String type, String name, String value)
    {
        this.type = type;
        this.name = name;

        // see if it can be formatted as a long with commas at thousands
        try
        {
            value = DF.format(Long.parseLong(value));
        }
        catch (NumberFormatException nfe)
        {
        }
        
        this.value = value;
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
