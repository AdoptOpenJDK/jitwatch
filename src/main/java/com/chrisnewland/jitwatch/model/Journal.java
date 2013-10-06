package com.chrisnewland.jitwatch.model;

import java.util.ArrayList;
import java.util.List;

public class Journal
{
    // writes dominate so not COWAL
    private List<String> entryList;
    
    public Journal()
    {
        entryList = new ArrayList<>();
    }
    
    public synchronized void addEntry(String entry)
    {
        entryList.add(entry);
    }
    
    public synchronized List<String> getEntryList()
    {
        List<String> copy = new ArrayList<>(entryList);
        
        return copy;
    }
}
