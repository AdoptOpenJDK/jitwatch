package com.chrisnewland.jitwatch.core;

import com.chrisnewland.jitwatch.model.IMetaMember;

public interface IMemberFinder
{
	IMetaMember findMemberWithSignature(String logSignature);
}
