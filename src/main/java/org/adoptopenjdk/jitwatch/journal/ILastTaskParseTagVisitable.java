package org.adoptopenjdk.jitwatch.journal;

import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;

public interface ILastTaskParseTagVisitable
{
	void visitParseTag(Tag parseTag, IParseDictionary parseDictionary) throws LogParseException;
}