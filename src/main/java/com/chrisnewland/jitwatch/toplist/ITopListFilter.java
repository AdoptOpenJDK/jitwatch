package com.chrisnewland.jitwatch.toplist;

import com.chrisnewland.jitwatch.model.IMetaMember;

public interface ITopListFilter
{
    public boolean acceptMember(IMetaMember mm);
    
    public MemberScore getScore(IMetaMember mm);
}