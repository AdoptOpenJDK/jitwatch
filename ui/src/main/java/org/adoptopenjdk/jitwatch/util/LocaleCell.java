/*
 * Copyright (c) 2013-2021 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */

package org.adoptopenjdk.jitwatch.util;

import javafx.scene.control.ListCell;

import java.util.Locale;

public class LocaleCell extends ListCell<Locale> {
    @Override
    public void updateItem(Locale locale, boolean empty) {
        super.updateItem(locale, empty);
        if (empty) {
            setText(null);
        } else {
            setText(locale.getDisplayLanguage(locale));
        }
    }
}