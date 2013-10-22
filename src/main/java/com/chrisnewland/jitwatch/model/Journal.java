package com.chrisnewland.jitwatch.model;

import java.util.ArrayList;
import java.util.List;

import com.chrisnewland.jitwatch.core.Tag;

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
