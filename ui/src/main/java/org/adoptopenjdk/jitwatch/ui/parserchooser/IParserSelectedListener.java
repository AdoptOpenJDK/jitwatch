package org.adoptopenjdk.jitwatch.ui.parserchooser;

import org.adoptopenjdk.jitwatch.parser.ParserType;

public interface IParserSelectedListener
{
	void parserSelected(ParserType parserType);
}