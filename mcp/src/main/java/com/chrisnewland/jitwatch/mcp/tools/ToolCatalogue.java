package com.chrisnewland.jitwatch.mcp.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.chrisnewland.jitwatch.mcp.CallLog;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * The tools this server offers, and the one path every call takes. Timing, logging,
 * serialising and error mapping happen here once rather than in every tool.
 */
public final class ToolCatalogue {

	private final List<Tool> tools;

	private final McpJsonMapper mapper;

	private final CallLog log;

	public ToolCatalogue(ToolContext context, McpJsonMapper mapper, CallLog log) {
		this.tools = tools(context);
		this.mapper = mapper;
		this.log = log;
	}

	public static List<Tool> tools(ToolContext context) {
		return List.of(new LoadLogTool(context), new CheckupTool(context), new FindMethodsTool(context),
				new ExplainMethodTool(context), new ExplainCallSiteTool(context), new InliningTreeTool(context),
				new TopListTool(context), new DeoptsTool(context), new ListSessionsTool(context));
	}

	public List<McpServerFeatures.SyncToolSpecification> specifications() {
		List<McpServerFeatures.SyncToolSpecification> list = new ArrayList<>();

		for (Tool tool : tools) {
			list.add(specification(tool));
		}

		return list;
	}

	/**
	 * A bad argument comes back as a tool error carrying the tool's own message, because
	 * the caller can act on that. Anything else is a fault and is logged as one.
	 */
	private McpServerFeatures.SyncToolSpecification specification(Tool tool) {
		String name = tool.name();

		McpSchema.Tool declaration = McpSchema.Tool.builder(name, tool.schema())
			.title(tool.title())
			.description(tool.description())
			.build();

		BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler = (exchange,
				request) -> {
			ToolArguments arguments = new ToolArguments(request.arguments());

			long startedAt = System.currentTimeMillis();

			log.started(name, arguments.isEmpty() ? null : arguments);

			try {
				Map<String, Object> payload = tool.call(arguments).toMap();

				String json = mapper.writeValueAsString(payload);

				log.succeeded(name, System.currentTimeMillis() - startedAt, json.length());

				return McpSchema.CallToolResult.builder()
					.addTextContent(json)
					.structuredContent(payload)
					.isError(false)
					.build();
			}
			catch (IllegalArgumentException iae) {
				log.rejected(name, System.currentTimeMillis() - startedAt, iae.getMessage());

				return error(iae.getMessage());
			}
			catch (Exception e) {
				String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();

				log.failed(name, System.currentTimeMillis() - startedAt, message, e);

				return error(message);
			}
		};

		return new McpServerFeatures.SyncToolSpecification(declaration, handler);
	}

	private static McpSchema.CallToolResult error(String message) {
		return McpSchema.CallToolResult.builder().addTextContent(message).isError(true).build();
	}

}
