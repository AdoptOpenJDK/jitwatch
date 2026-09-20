package com.chrisnewland.jitwatch.mcp.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.mcp.answer.Answer;
import com.chrisnewland.jitwatch.mcp.session.Session;

public final class ListSessionsTool implements Tool {

	private final ToolContext context;

	public ListSessionsTool(ToolContext context) {
		this.context = context;
	}

	@Override
	public String name() {
		return "list_sessions";
	}

	@Override
	public String title() {
		return "Logs currently loaded";
	}

	@Override
	public String description() {
		return "List the sessions this server holds, with the log each one came from.";
	}

	@Override
	public java.util.Map<String, Object> schema() {
		return Schemas.object().build();
	}

	@Override
	public Answer call(ToolArguments arguments) throws Exception {
		return listSessions();
	}

	public Answer listSessions() {
		List<Map<String, Object>> rows = new ArrayList<>();

		for (Session session : context.sessions().all()) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("session_id", session.getId());
			row.put("log", session.getLogPath());
			row.put("jdk_major", session.getThresholds().getJdkMajor());
			rows.add(row);
		}

		Answer answer = new Answer();
		answer.put("sessions", rows);
		answer.plain(rows.isEmpty() ? "No log is loaded. Call load_log first." : rows.size() + " session(s) loaded.");
		return answer;
	}

}
