package com.chrisnewland.jitwatch.mcp.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.model.IMetaMember;

import com.chrisnewland.jitwatch.mcp.answer.Answer;
import com.chrisnewland.jitwatch.mcp.query.QueryService;
import com.chrisnewland.jitwatch.mcp.session.Session;

public final class FindMethodsTool implements Tool {

	private final ToolContext context;

	public FindMethodsTool(ToolContext context) {
		this.context = context;
	}

	@Override
	public String name() {
		return "find_methods";
	}

	@Override
	public String title() {
		return "Search for methods by name";
	}

	@Override
	public String description() {
		return "Fuzzy search over every method the compiler touched. Use this to turn a name a person typed into the signature the other tools want.";
	}

	@Override
	public java.util.Map<String, Object> schema() {
		return Schemas.object()
			.reqStr("session_id", "Session id from load_log")
			.reqStr("query", "Part of a method or class name, for example (bigMethod) or (Order.total)")
			.bool("compiled_only", "Only methods that were compiled. Default true.")
			.integer("limit", "How many matches to return")
			.build();
	}

	@Override
	public Answer call(ToolArguments arguments) throws Exception {
		return findMethods(arguments.string("session_id"), arguments.string("query"), arguments.bool("compiled_only"),
				arguments.integer("limit"));
	}

	public Answer findMethods(String sessionId, String query, Boolean compiledOnly, Integer limit) throws Exception {
		Session session = context.requireSession(sessionId);

		int effectiveLimit = context.budget().clampLimit(limit);

		List<IMetaMember> members = session.getIndex()
			.find(query, compiledOnly == null || compiledOnly, effectiveLimit + 1);

		List<Map<String, Object>> rows = new ArrayList<>();

		for (IMetaMember member : members) {
			rows.add(QueryService.memberSummary(member));
		}

		Answer answer = new Answer();

		answer.put("matches", context.budget().capList(rows, effectiveLimit, "matches", answer.getEvidence()));
		answer.put("query", query);

		if (rows.isEmpty()) {
			answer.plain("No method in this log matches \"" + query
					+ "\". Only methods the JIT compiler touched appear in a compilation log, so a method that was "
					+ "never hot will not be here.");
		}
		else {
			answer.plain("Found " + rows.size() + " matching method" + (rows.size() == 1 ? "" : "s") + ". "
					+ "Pass a signature to explain_method for the full story of one.");
		}

		return answer;
	}

}
