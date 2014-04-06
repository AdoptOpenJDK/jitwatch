/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import java.io.InputStream;

import javafx.scene.image.Image;

public class UserInterfaceUtil
{
    // icon from https://www.iconfinder.com/icons/173960/tick_icon#size=16
    public static Image TICK = null;

    static
    {
        // images directory added to jar by ant and mvn
        // If you want them to load when launching from IDE then put
        // src/main/resources on the IDE runtime classpath
        InputStream is = UserInterfaceUtil.class.getResourceAsStream("/images/tick.png");

        if (is != null)
        {
            TICK = new Image(is);
        }
        else
        {
        	//TODO make this a dialog, format too easy to miss in an IDE
            System.err.format("If running in an IDE please add src/main/resources to your classpath");
        }
    }
}
