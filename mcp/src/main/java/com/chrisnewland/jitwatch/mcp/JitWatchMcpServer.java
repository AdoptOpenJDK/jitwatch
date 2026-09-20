package com.chrisnewland.jitwatch.mcp;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.chrisnewland.freelogj.LoggerFactory;
import com.chrisnewland.jitwatch.mcp.answer.Budget;
import com.chrisnewland.jitwatch.mcp.session.SessionStore;
import com.chrisnewland.jitwatch.mcp.tools.ToolCatalogue;
import com.chrisnewland.jitwatch.mcp.tools.ToolContext;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;

/**
 * A Model Context Protocol server over JITWatch's HotSpot compilation log analysis.
 *
 * This class only starts things. The answers come from jitwatch-core by way of
 * {@link ToolContext}; which tools exist is {@link ToolCatalogue}.
 */
public final class JitWatchMcpServer {

	private static final String NAME = "jitwatch";

	static final String VERSION = "1.5";

	private static final String HTTP_ENDPOINT = "/mcp";

	/** Tools only: this server exposes no resources, prompts or subscriptions. */
	private static final McpSchema.ServerCapabilities CAPABILITIES = McpSchema.ServerCapabilities.builder()
		.tools(false)
		.build();

	private static final String INSTRUCTIONS = """
			JITWatch exposes what the HotSpot JIT compiler did to a Java program, read from a log \
			written with -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation.

			Start with load_log, then checkup. Every answer has two halves: 'plain' is written for \
			someone who does not know HotSpot internals, 'evidence' has the exact compile ids, \
			bytecode indices, reason strings and the compiler flag and limit that applied. Show \
			whichever half suits the person you are helping.

			A compilation log records what the compiler decided, never how long anything took. \
			Findings explain measurements; they are not measurements. When nothing here explains a \
			slowdown, say so and suggest a profiler, garbage collection logs or lock analysis instead.
			""";

	private final ServerConfig config;

	private final CallLog log;

	private JitWatchMcpServer(ServerConfig config, CallLog log) {
		this.config = config;
		this.log = log;
	}

	public static void main(String[] args) throws Exception {
		if (List.of(args).contains("--help")) {
			System.err.println(ServerConfig.usage());
			return;
		}

		// stdout is the protocol under stdio, so keep a private handle and point
		// System.out at stderr: nothing that prints can then corrupt a JSON-RPC frame.
		PrintStream protocolOut = System.out;
		PrintStream stderr = new PrintStream(new FileOutputStream(FileDescriptor.err), true);
		System.setOut(stderr);

		ServerConfig config = ServerConfig.parse(args);

		// Logging joins the call trace on stderr unless a file was asked for, so it lands
		// wherever the server was started rather than in an unfindable temp file.
		LoggerFactory.initialise(config.logLevel(), stderr);

		if (config.logFile() != null) {
			LoggerFactory.setLogFile(config.logFile());
		}

		CallLog log = new CallLog(stderr, config.wantsStackTraces());

		for (String warning : config.warnings()) {
			log.note(warning);
		}

		new JitWatchMcpServer(config, log).run(protocolOut);
	}

	private void run(PrintStream protocolOut) throws Exception {
		SessionStore sessions = new SessionStore(config.maxSessions());
		Budget budget = new Budget(config.maxListItems(), 2_000, config.maxAnswerChars());
		ToolContext context = new ToolContext(sessions, budget, config.allowedRoots());

		McpJsonMapper jsonMapper = McpJsonDefaults.getMapper();

		List<McpServerFeatures.SyncToolSpecification> specifications = new ToolCatalogue(context, jsonMapper, log)
			.specifications();

		McpSyncServer server;
		String transport;

		if (config.isHttp()) {
			// Outlives any one client, so several can connect and every call prints on
			// this console: run it from an IDE and watch real traffic under a debugger.
			HttpServletStreamableServerTransportProvider http = HttpServletStreamableServerTransportProvider.builder()
				.jsonMapper(jsonMapper)
				.mcpEndpoint(HTTP_ENDPOINT)
				.build();

			server = McpServer.sync(http)
				.serverInfo(NAME, VERSION)
				.instructions(INSTRUCTIONS)
				.capabilities(CAPABILITIES)
				.tools(specifications)
				.build();

			serve(http, config.httpPort());

			transport = "http://localhost:" + config.httpPort() + HTTP_ENDPOINT;
		}
		else {
			StdioServerTransportProvider stdio = new StdioServerTransportProvider(jsonMapper, System.in, protocolOut);

			server = McpServer.sync(stdio)
				.serverInfo(NAME, VERSION)
				.instructions(INSTRUCTIONS)
				.capabilities(CAPABILITIES)
				.tools(specifications)
				.build();

			transport = "stdio";
		}

		log.note("ready. " + config.describe(transport));
		log.note(specifications.size() + " tools registered. Waiting for calls.");

		Runtime.getRuntime().addShutdownHook(new Thread(server::closeGracefully));

		// The transport serves on its own threads; park until told to stop.
		new CountDownLatch(1).await();
	}

	/**
	 * Mounts the MCP servlet in an embedded Jetty, because the HTTP transport is a
	 * servlet.
	 */
	private static void serve(HttpServletStreamableServerTransportProvider transport, int port) throws Exception {
		Server jetty = new Server(port);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(new ServletHolder(transport), HTTP_ENDPOINT);

		jetty.setHandler(context);
		jetty.start();
	}

}
