/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import java.util.ArrayList;
import java.util.List;

public class Journal
{
    // writes dominate so not COWAL
    private List<Tag> entryList;
    
    public Journal()
    {
        entryList = new ArrayList<>();
    }
    
    public synchronized void addEntry(Tag entry)
    {
        entryList.add(entry);
    }
    
    public synchronized List<Tag> getEntryList()
    {
        List<Tag> copy = new ArrayList<>(entryList);
        
        return copy;
    }
}
