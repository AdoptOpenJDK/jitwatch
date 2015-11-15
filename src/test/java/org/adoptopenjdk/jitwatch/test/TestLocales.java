/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.*;

import java.util.Locale;

import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.junit.Test;

public class TestLocales
{
    @Test
    public void testParseStampWithSeparatorDotForDotLocale()
    {
        Locale.setDefault(Locale.UK);

        String stamp = "1.234";
        long stampMillis = ParseUtil.parseStamp(stamp);
        assertEquals(1234, stampMillis);
    }

    @Test
    public void testParseStampWithSeparatorDotForNonDotLocale()
    {
        Locale.setDefault(Locale.FRANCE);

        String stamp = "1,234";
        long stampMillis = ParseUtil.parseStamp(stamp);
        assertEquals(1234, stampMillis);
    }
}
