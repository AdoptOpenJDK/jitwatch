package com.chrisnewland.jitwatch.mcp.tools;

import java.util.Map;

import com.chrisnewland.jitwatch.mcp.answer.Answer;

/**
 * One question this server can answer. A tool declares itself and answers a call, so its
 * schema and the code reading that schema cannot drift apart.
 *
 * Nothing here knows about JSON-RPC. {@link ToolCatalogue} handles the protocol, timing,
 * logging and errors once for all of them.
 */
public interface Tool {

	String name();

	String title();

	String description();

	Map<String, Object> schema();

	Answer call(ToolArguments arguments) throws Exception;

}
