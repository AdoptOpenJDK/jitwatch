/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.toplist;

import com.chrisnewland.jitwatch.model.IMetaMember;

public class CompiledAttributeFilterAdapter implements ITopListFilter
{
    private final String attr;

    public CompiledAttributeFilterAdapter(String attr)
    {
        this.attr = attr;
    }
    
    @Override
    public MemberScore getScore(IMetaMember mm)
    {
        long value = Long.valueOf(mm.getCompiledAttribute(attr));
        return new MemberScore(mm, value);
    }
    
    @Override
    public boolean acceptMember(IMetaMember mm)
    {
        return mm.getCompiledAttribute(attr) != null;
    }
}
