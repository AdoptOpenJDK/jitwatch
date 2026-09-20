package com.chrisnewland.jitwatch.mcp.query;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.chain.CompileChainWalker;
import com.chrisnewland.jitwatch.chain.CompileNode;
import com.chrisnewland.jitwatch.model.Compilation;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.bytecode.BCAnnotationType;
import com.chrisnewland.jitwatch.model.bytecode.BytecodeAnnotationBuilder;
import com.chrisnewland.jitwatch.model.bytecode.BytecodeAnnotationList;
import com.chrisnewland.jitwatch.model.bytecode.BytecodeAnnotations;
import com.chrisnewland.jitwatch.model.bytecode.LineAnnotation;
import com.chrisnewland.jitwatch.model.bytecode.LineTable;
import com.chrisnewland.jitwatch.model.bytecode.MemberBytecode;

import com.chrisnewland.jitwatch.mcp.session.Session;
import com.chrisnewland.jitwatch.mcp.threshold.InlineReasons;
import com.chrisnewland.jitwatch.mcp.threshold.Thresholds;

/**
 * Turns JITWatch's model into the answers the tools serve.
 *
 * Nothing here re-implements analysis: the parsing, the inlining trees and the bytecode
 * annotations all come from jitwatch-core. This class resolves what those mean against
 * the flags that were in effect, and shapes the result so it fits in an agent's context.
 */
public final class QueryService {

	/**
	 * How the JITWatch tooltip text is laid out; see
	 * TooltipUtil.buildInlineAnnotationText.
	 */
	private static final String PREFIX_CLASS = "Class: ";

	private static final String PREFIX_METHOD = "Method: ";

	private static final String PREFIX_INLINED = "Inlined: ";

	private static final String PREFIX_COUNT = "Count: ";

	private static final String PREFIX_BYTES = "Bytes: ";

	private QueryService() {
	}

	/**
	 * The compiler that produced a compilation, as C1 or C2 alone.
	 *
	 * jitwatch-core reports "C2 OSR" for an on-stack-replacement compilation, which folds
	 * two facts into one string. The server keeps them apart: compiler says which
	 * compiler, and a separate osr flag says how it was entered.
	 */
	public static String compilerOf(Compilation compilation) {
		String raw = compilation.getCompiler();

		if (raw == null) {
			return null;
		}

		int space = raw.indexOf(' ');

		return space > 0 ? raw.substring(0, space) : raw;
	}

	public static String signatureOf(IMetaMember member) {
		return member.getMetaClass().getFullyQualifiedName() + "." + member.toStringUnqualifiedMethodName(false, false);
	}

	public static Map<String, Object> memberSummary(IMetaMember member) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("signature", signatureOf(member));
		map.put("class", member.getMetaClass().getFullyQualifiedName());
		map.put("compiled", member.isCompiled());
		map.put("compilations", member.getCompilations().size());

		Integer bytecodeSize = bytecodeSize(member);

		if (bytecodeSize != null) {
			map.put("bytecode_size", bytecodeSize);
		}

		Compilation last = member.getLastCompilation();

		if (last != null) {
			map.put("last_compiler", compilerOf(last));
			map.put("last_tier", last.getLevel());
			map.put("last_native_size", last.getNativeSize());
		}

		long heat = heatOf(member);

		if (heat > 0) {
			map.put("invocations", heat);
		}

		return map;
	}

	/**
	 * Bytecode size from the log's own attributes, which needs no mounted class files.
	 */
	public static Integer bytecodeSize(IMetaMember member) {
		for (Compilation compilation : member.getCompilations()) {
			int size = compilation.getBytecodeSize();

			if (size > 0) {
				return size;
			}
		}

		Integer fromAttribute = intAttribute(member.getCompiledAttribute("bytes"));

		if (fromAttribute != null) {
			return fromAttribute;
		}

		return intAttribute(member.getQueuedAttribute("bytes"));
	}

	/**
	 * How hot the member is, as the highest invocation count the compiler recorded for
	 * it.
	 *
	 * Used to rank findings: the same refusal on a method called ten times and one called
	 * ten million times are not the same problem.
	 */
	public static long heatOf(IMetaMember member) {
		long best = 0;

		for (Compilation compilation : member.getCompilations()) {
			best = Math.max(best, longAttribute(compilation.getQueuedAttribute("count")));
			best = Math.max(best, longAttribute(compilation.getQueuedAttribute("iicount")));
			best = Math.max(best, longAttribute(compilation.getQueuedAttribute("backedge_count")));
		}

		return best;
	}

	public static Map<String, Object> compilationSummary(Compilation compilation) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("compile_id", compilation.getCompileID());
		map.put("compiler", compilerOf(compilation));
		map.put("tier", compilation.getLevel());
		map.put("osr", compilation.isOSR());

		if (compilation.isOSR()) {
			map.put("osr_bci", compilation.getOSRBCI());
		}

		map.put("native_size", compilation.getNativeSize());
		map.put("bytecode_size", compilation.getBytecodeSize());
		map.put("queued_ms", compilation.getStampTaskQueued());
		map.put("emitted_ms", compilation.getStampNMethodEmitted());
		map.put("duration_ms", compilation.getCompilationDuration());

		if (compilation.isFailed()) {
			map.put("failed", true);
		}

		Integer inlinedBytes = intAttribute(compilation.getCompiledAttribute("inlined_bytes"));

		if (inlinedBytes != null) {
			map.put("inlined_bytes", inlinedBytes);
		}

		Integer decompiles = intAttribute(compilation.getQueuedAttribute("decompiles"));

		if (decompiles != null && decompiles > 0) {
			map.put("decompiles_before_this", decompiles);
		}

		return map;
	}

	/**
	 * Every inlining, intrinsic and virtual-call decision inside one member, across all
	 * of its compilations.
	 * @param bciFilter when >= 0, only decisions at this bytecode index
	 */
	public static List<Decision> decisionsIn(Session session, IMetaMember member, int bciFilter) {
		List<Decision> decisions = new ArrayList<>();

		IReadOnlyJITDataModel model = session.getModel();

		LineTable lineTable = lineTableOf(session, member);

		for (Compilation compilation : member.getCompilations()) {
			BytecodeAnnotations annotations;

			try {
				annotations = new BytecodeAnnotationBuilder(false).buildBytecodeAnnotations(member,
						compilation.getIndex(), model);
			}
			catch (Exception e) {
				// A compilation whose annotations cannot be built (no mounted bytecode,
				// or a tag
				// shape this JDK writes differently) must not sink the whole answer
				continue;
			}

			BytecodeAnnotationList list = annotations.getAnnotationList(member);

			if (list == null) {
				continue;
			}

			for (Map.Entry<Integer, List<LineAnnotation>> entry : list.getEntries()) {
				int bci = entry.getKey();

				if (bciFilter >= 0 && bci != bciFilter) {
					continue;
				}

				for (LineAnnotation annotation : entry.getValue()) {
					Decision decision = toDecision(annotation, bci, compilation, session.getThresholds());

					if (decision != null) {
						decision.line = sourceLineFor(lineTable, bci);
						decisions.add(decision);
					}
				}
			}
		}

		decisions.sort(Comparator.comparingInt((Decision d) -> d.bci).thenComparing(d -> d.compileId));

		return decisions;
	}

	private static Decision toDecision(LineAnnotation annotation, int bci, Compilation compilation,
			Thresholds thresholds) {
		BCAnnotationType type = annotation.getType();

		if (type != BCAnnotationType.INLINE_SUCCESS && type != BCAnnotationType.INLINE_FAIL
				&& type != BCAnnotationType.INTRINSIC_USED && type != BCAnnotationType.VIRTUAL_CALL) {
			return null;
		}

		Decision decision = new Decision();
		decision.bci = bci;
		decision.compileId = compilation.getCompileID();
		decision.compiler = compilerOf(compilation);
		decision.tier = compilation.getLevel();
		decision.osr = compilation.isOSR();

		String text = annotation.getAnnotation();

		Parsed parsed = parseAnnotation(text);

		decision.callee = parsed.callee();
		decision.calleeBytecodeSize = parsed.bytes();
		decision.callCount = parsed.count();

		switch (type) {
			case INLINE_SUCCESS:
				decision.outcome = Decision.INLINED;
				decision.reason = parsed.reason();
				decision.reasonNormalised = InlineReasons.INLINED;
				decision.plain = "The compiler copied this callee into the caller, so no call happens at run time.";
				break;

			case INTRINSIC_USED:
				decision.outcome = Decision.INTRINSIC;
				decision.reason = text == null ? null : text.trim();
				decision.reasonNormalised = "INTRINSIC";
				decision.plain = "The compiler replaced this call with a hand-written machine-code implementation.";
				break;

			case VIRTUAL_CALL:
				decision.outcome = Decision.VIRTUAL;
				decision.reasonNormalised = InlineReasons.VIRTUAL_CALL;
				decision.plain = "This call stayed a real virtual dispatch: the target is decided at run time.";
				break;

			case INLINE_FAIL:
			default:
				decision.outcome = Decision.NOT_INLINED;
				decision.reason = parsed.reason();

				InlineReasons.Reason reason = InlineReasons.lookup(parsed.reason());

				decision.reasonNormalised = reason.category();
				decision.flag = reason.flag();
				decision.limit = InlineReasons.limitFor(reason, thresholds);
				decision.plain = reason.plain();

				if (InlineReasons.CALLEE_TOO_LARGE.equals(reason.category()) && parsed.bytes() != null) {
					decision.actual = parsed.bytes().longValue();
				}

				break;
		}

		return decision;
	}

	private record Parsed(String callee, String reason, Integer bytes, Long count) {
	}

	/** Reads back the tooltip text JITWatch builds for an inlining annotation. */
	private static Parsed parseAnnotation(String text) {
		if (text == null) {
			return new Parsed(null, null, null, null);
		}

		String klass = null;
		String method = null;
		String reason = null;
		Integer bytes = null;
		Long count = null;

		for (String line : text.split("\n")) {
			String trimmed = line.trim();

			if (trimmed.startsWith(PREFIX_CLASS)) {
				klass = trimmed.substring(PREFIX_CLASS.length()).trim();
			}
			else if (trimmed.startsWith(PREFIX_METHOD)) {
				method = trimmed.substring(PREFIX_METHOD.length()).trim();
			}
			else if (trimmed.startsWith(PREFIX_INLINED)) {
				String rest = trimmed.substring(PREFIX_INLINED.length()).trim();

				int comma = rest.indexOf(',');

				reason = comma >= 0 ? rest.substring(comma + 1).trim() : rest;
			}
			else if (trimmed.startsWith(PREFIX_BYTES)) {
				bytes = intAttribute(trimmed.substring(PREFIX_BYTES.length()).trim());
			}
			else if (trimmed.startsWith(PREFIX_COUNT)) {
				long parsedCount = longAttribute(trimmed.substring(PREFIX_COUNT.length()).trim());
				count = parsedCount > 0 ? parsedCount : null;
			}
		}

		String callee = null;

		if (klass != null && method != null) {
			callee = klass + "." + method;
		}
		else if (method != null) {
			callee = method;
		}

		return new Parsed(callee, reason, bytes, count);
	}

	/**
	 * Speculation points the compiler planted, and how often the method was thrown away
	 * before.
	 */
	public static List<Map<String, Object>> deoptsIn(Session session, IMetaMember member) {
		List<Map<String, Object>> out = new ArrayList<>();

		LineTable lineTable = lineTableOf(session, member);

		for (Compilation compilation : member.getCompilations()) {
			BytecodeAnnotations annotations;

			try {
				annotations = new BytecodeAnnotationBuilder(false).buildBytecodeAnnotations(member,
						compilation.getIndex(), session.getModel());
			}
			catch (Exception e) {
				continue;
			}

			BytecodeAnnotationList list = annotations.getAnnotationList(member);

			if (list == null) {
				continue;
			}

			for (Map.Entry<Integer, List<LineAnnotation>> entry : list.getEntries()) {
				for (LineAnnotation annotation : entry.getValue()) {
					if (annotation.getType() != BCAnnotationType.UNCOMMON_TRAP) {
						continue;
					}

					Map<String, Object> trap = new LinkedHashMap<>();
					trap.put("bci", entry.getKey());

					int line = sourceLineFor(lineTable, entry.getKey());

					if (line > 0) {
						trap.put("line", line);
					}

					trap.put("detail", annotation.getAnnotation() == null ? null : annotation.getAnnotation().trim());
					trap.put("in_compilation", compilation.getCompileID());
					trap.put("compiler", compilerOf(compilation));
					out.add(trap);
				}
			}
		}

		return out;
	}

	/**
	 * Highest "decompiles" count recorded for the member: how often HotSpot threw its
	 * code away.
	 */
	public static int decompileCount(IMetaMember member) {
		int best = 0;

		for (Compilation compilation : member.getCompilations()) {
			Integer decompiles = intAttribute(compilation.getQueuedAttribute("decompiles"));

			if (decompiles != null) {
				best = Math.max(best, decompiles);
			}
		}

		return best;
	}

	public static Map<String, Object> inliningTree(Session session, Compilation compilation, int maxDepth,
			int[] prunedCounter) {
		CompileNode root = new CompileChainWalker(session.getModel()).buildCallTree(compilation);

		if (root == null) {
			return null;
		}

		return nodeToMap(root, 0, maxDepth, prunedCounter);
	}

	private static Map<String, Object> nodeToMap(CompileNode node, int depth, int maxDepth, int[] prunedCounter) {
		Map<String, Object> map = new LinkedHashMap<>();

		IMetaMember member = node.getMember();

		String callee = member != null ? signatureOf(member) : node.getMemberName();

		if (callee == null || callee.isBlank()) {
			// A virtual call whose target the compiler could not name. Saying so is more
			// useful
			// than a null, because "the target is unknown" is the finding.
			callee = node.isVirtualCall() ? "unknown target (virtual call)" : "unknown";
		}

		map.put("callee", callee);

		if (depth > 0) {
			map.put("inlined", node.isInlined());

			if (node.isVirtualCall()) {
				map.put("virtual_call", true);
			}
		}

		String tooltip = node.getTooltipText();

		if (tooltip != null && !tooltip.isEmpty()) {
			Parsed parsed = parseAnnotation(tooltip);

			if (parsed.reason() != null) {
				map.put("reason", parsed.reason());

				if (node.isInlined()) {
					map.put("reason_normalised", InlineReasons.INLINED);
				}
				else {
					map.put("reason_normalised", InlineReasons.lookup(parsed.reason()).category());
				}
			}

			if (parsed.bytes() != null) {
				map.put("callee_bytecode_size", parsed.bytes());
			}
		}

		List<CompileNode> children = node.getChildren();

		if (!children.isEmpty()) {
			if (depth >= maxDepth) {
				prunedCounter[0] += countTree(node) - 1;
				map.put("children_pruned", children.size());
			}
			else {
				List<Object> kids = new ArrayList<>();

				for (CompileNode child : children) {
					kids.add(nodeToMap(child, depth + 1, maxDepth, prunedCounter));
				}

				map.put("children", kids);
			}
		}

		return map;
	}

	private static int countTree(CompileNode node) {
		int total = 1;

		for (CompileNode child : node.getChildren()) {
			total += countTree(child);
		}

		return total;
	}

	public static Compilation compilationById(IMetaMember member, String compileId) {
		if (compileId == null || compileId.isBlank()) {
			return member.getLastCompilation();
		}

		for (Compilation compilation : member.getCompilations()) {
			if (compileId.equals(compilation.getCompileID())) {
				return compilation;
			}
		}

		return null;
	}

	/**
	 * The compilation that best represents what a method became.
	 *
	 * A method whose loops were compiled on-stack has one compilation per loop, so "the
	 * last one" is whichever loop happened to warm up last and says nothing about the
	 * rest of the method. Prefer the highest tier, then the one that absorbed the most
	 * callee bytecode, then the largest body of machine code. Callers who want a specific
	 * one pass compile_id.
	 */
	public static Compilation bestCompilation(IMetaMember member) {
		Compilation best = null;

		for (Compilation candidate : member.getCompilations()) {
			if (best == null || score(candidate) > score(best)) {
				best = candidate;
			}
		}

		return best;
	}

	private static long score(Compilation compilation) {
		Integer inlinedBytes = intAttribute(compilation.getCompiledAttribute("inlined_bytes"));

		long tier = compilation.getLevel();

		return tier * 100_000_000L + (inlinedBytes == null ? 0 : inlinedBytes) * 1_000L + compilation.getNativeSize();
	}

	public static LineTable lineTableOf(Session session, IMetaMember member) {
		MemberBytecode bytecode = session.getBytecodeCache().bytecodeFor(member);

		try {
			return bytecode == null ? null : bytecode.getLineTable();
		}
		catch (Exception e) {
			return null;
		}
	}

	public static int sourceLineFor(LineTable lineTable, int bci) {
		if (lineTable == null) {
			return -1;
		}

		try {
			return lineTable.findSourceLineForBytecodeOffset(bci);
		}
		catch (Exception e) {
			return -1;
		}
	}

	public static List<Integer> bciRangeForLine(Session session, IMetaMember member, int line) {
		List<Integer> out = new ArrayList<>();

		LineTable lineTable = lineTableOf(session, member);

		MemberBytecode bytecode = session.getBytecodeCache().bytecodeFor(member);

		if (lineTable == null || bytecode == null) {
			return out;
		}

		for (com.chrisnewland.jitwatch.model.bytecode.BytecodeInstruction instruction : bytecode.getInstructions()) {
			if (sourceLineFor(lineTable, instruction.getOffset()) == line) {
				out.add(instruction.getOffset());
			}
		}

		return out;
	}

	public static Map<String, Object> sourceLocation(Session session, IMetaMember member) {
		MetaClass metaClass = member.getMetaClass();

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("class", metaClass.getFullyQualifiedName());

		LineTable lineTable = lineTableOf(session, member);

		if (lineTable != null && !lineTable.getEntries().isEmpty()) {
			int[] range = lineTable.getSourceRange();

			if (range != null && range.length == 2 && range[0] > 0) {
				map.put("first_line", range[0]);
				map.put("last_line", range[1]);
			}
		}

		return map;
	}

	public static Integer intAttribute(String value) {
		if (value == null) {
			return null;
		}

		try {
			return Integer.valueOf(value.trim());
		}
		catch (NumberFormatException nfe) {
			return null;
		}
	}

	public static long longAttribute(String value) {
		if (value == null) {
			return 0;
		}

		try {
			return Long.parseLong(value.trim());
		}
		catch (NumberFormatException nfe) {
			return 0;
		}
	}

}
