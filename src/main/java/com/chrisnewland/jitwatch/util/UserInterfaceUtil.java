/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import java.io.InputStream;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserInterfaceUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UserInterfaceUtil.class);

    // icon from https://www.iconfinder.com/icons/173960/tick_icon#size=16
    private static Image tick = null;

    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private UserInterfaceUtil() {
    }

    static
    {
        // images directory added to jar by ant and mvn
        // If you want them to load when launching from IDE then put
        // src/main/resources on the IDE runtime classpath
        InputStream is = UserInterfaceUtil.class.getResourceAsStream("/images/tick.png");

        if (is != null)
        {
            tick = new Image(is);
        }
        else
        {
        	//TODO make this a dialog, format too easy to miss in an IDE
            LOGGER.error("If running in an IDE please add src/main/resources to your classpath");
        }
    }

    public static Image getTick() {
        return tick;
    }
}
