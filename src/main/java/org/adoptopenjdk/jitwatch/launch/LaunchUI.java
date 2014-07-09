/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.launch;

import org.adoptopenjdk.jitwatch.ui.JITWatchUI;

public final class LaunchUI
{
    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private LaunchUI() {
    }

    public static void main(String[] args)
	{
		new JITWatchUI(args);
	}
}
