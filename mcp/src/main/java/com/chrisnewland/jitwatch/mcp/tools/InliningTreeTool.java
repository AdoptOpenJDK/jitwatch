package com.chrisnewland.jitwatch.mcp.tools;

import java.util.Map;

import com.chrisnewland.jitwatch.model.Compilation;
import com.chrisnewland.jitwatch.model.IMetaMember;

import com.chrisnewland.jitwatch.mcp.answer.Answer;
import com.chrisnewland.jitwatch.mcp.findings.FindingsEngine;
import com.chrisnewland.jitwatch.mcp.query.QueryService;
import com.chrisnewland.jitwatch.mcp.session.Session;

public final class InliningTreeTool implements Tool {

	private final ToolContext context;

	public InliningTreeTool(ToolContext context) {
		this.context = context;
	}

	@Override
	public String name() {
		return "get_inlining_tree";
	}

	@Override
	public String title() {
		return "What was folded into one compilation";
	}

	@Override
	public String description() {
		return "The tree of calls the compiler inlined into a compiled method, and the ones it refused, with the reason for each refusal. This is the shape of the machine code that actually runs.";
	}

	@Override
	public java.util.Map<String, Object> schema() {
		return Schemas.object()
			.reqStr("session_id", "Session id from load_log")
			.reqStr("signature", "The compiled method at the root of the tree")
			.str("compile_id", "Which compilation. Defaults to the highest tier one.")
			.integer("max_depth", "How deep to descend. Default 6, maximum 15.")
			.build();
	}

	@Override
	public Answer call(ToolArguments arguments) throws Exception {
		return inliningTree(arguments.string("session_id"), arguments.string("signature"),
				arguments.string("compile_id"), arguments.integer("max_depth"));
	}

	public Answer inliningTree(String sessionId, String signature, String compileId, Integer maxDepth)
			throws Exception {
		Session session = context.requireSession(sessionId);

		IMetaMember member = context.requireMember(session, signature);

		Compilation compilation = compileId == null || compileId.isBlank() ? QueryService.bestCompilation(member)
				: QueryService.compilationById(member, compileId);

		if (compilation == null) {
			throw new IllegalArgumentException(
					"No compilation " + compileId + " for " + QueryService.signatureOf(member));
		}

		int depth = maxDepth == null || maxDepth <= 0 ? 6 : Math.min(maxDepth, 15);

		int[] pruned = new int[1];

		Map<String, Object> tree = QueryService.inliningTree(session, compilation, depth, pruned);

		Answer answer = new Answer();

		answer.put("method", QueryService.signatureOf(member));
		answer.put("compilation", QueryService.compilationSummary(compilation));
		answer.put("max_depth", depth);

		int total = member.getCompilations().size();

		if (total > 1) {
			answer.put("other_compilations", total - 1);

			// A method whose loops were compiled on-stack has one compilation per loop,
			// so the
			// tree shown covers one loop and not the whole method
			answer.note("This method has " + total + " compilations. The tree above is for compile_id "
					+ compilation.getCompileID()
					+ ", chosen as the highest tier with the most inlined code. Pass compile_id to see another; "
					+ "explain_method lists them all.");
		}

		if (tree == null) {
			answer.plain("This compilation has no recorded call tree. That happens for compilations the log did not "
					+ "carry a full parse for, such as native wrappers.");
			return answer;
		}

		answer.put("tree", tree);

		if (pruned[0] > 0) {
			answer.put("pruned_nodes", pruned[0]);
			answer.note("The tree was cut off at depth " + depth + ", hiding " + pruned[0]
					+ " nodes. Raise max_depth to see more.");
		}

		Integer inlinedBytes = QueryService.intAttribute(compilation.getCompiledAttribute("inlined_bytes"));

		StringBuilder plain = new StringBuilder();
		plain.append("This is what ")
			.append(QueryService.compilerOf(compilation))
			.append(" folded into ")
			.append(FindingsEngine.shortName(QueryService.signatureOf(member)))
			.append(". ");

		if (inlinedBytes != null) {
			plain.append(inlinedBytes)
				.append(" bytes of callee bytecode became part of the ")
				.append(compilation.getNativeSize())
				.append("-byte compiled method. ");
		}

		plain.append("A node marked inlined has no call at run time; one that is not stayed a real call.");

		answer.plain(plain.toString());

		return answer;
	}

}
