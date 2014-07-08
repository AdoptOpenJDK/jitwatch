package org.adoptopenjdk.jitwatch.core;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

public interface IMemberFinder
{
	IMetaMember findMemberWithSignature(String logSignature);
}
