/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.paint.Color;

import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.CompilerName;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.model.LineAnnotation;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JournalUtil
{
	private static final Logger logger = LoggerFactory.getLogger(JournalUtil.class);

	/*
	 * Hide Utility Class Constructor Utility classes should not have a public
	 * or default constructor.
	 */

	private JournalUtil()
	{
	}

	public static Map<Integer, LineAnnotation> buildBytecodeAnnotations(Journal journal, List<BytecodeInstruction> instructions)
			throws AnnotationException
	{
		Map<Integer, LineAnnotation> result = new HashMap<>();

		if (journal != null)
		{
			CompilerName compilerName = getLastTaskCompiler(journal);

			Tag parsePhase = getParsePhase(journal);

			if (parsePhase != null)
			{
				List<Tag> parseTags = parsePhase.getNamedChildren(TAG_PARSE);

				for (Tag parseTag : parseTags)
				{
					buildParseTagAnnotations(parseTag, result, instructions, compilerName);
				}
			}
		}

		return result;
	}

	private static BytecodeInstruction getInstructionAtIndex(List<BytecodeInstruction> instructions, int index)
	{
		BytecodeInstruction found = null;

		for (BytecodeInstruction instruction : instructions)
		{
			if (instruction.getOffset() == index)
			{
				found = instruction;
				break;
			}
		}

		return found;
	}

	private static void buildParseTagAnnotations(Tag parseTag, Map<Integer, LineAnnotation> result,
			List<BytecodeInstruction> instructions, CompilerName compilerName) throws AnnotationException
	{
		List<Tag> children = parseTag.getChildren();

		int currentBytecode = -1;

		Map<String, String> methodAttrs = new HashMap<>();
		Map<String, String> callAttrs = new HashMap<>();

		boolean isC2 = false;

		if (compilerName == CompilerName.C2)
		{
			isC2 = true;
		}

		boolean inMethod = true;
		BytecodeInstruction currentInstruction = null;

		for (Tag child : children)
		{
			String name = child.getName();
			Map<String, String> tagAttrs = child.getAttrs();

			switch (name)
			{
			case TAG_BC:
			{
				String bciAttr = tagAttrs.get(ATTR_BCI);
				String codeAttr = tagAttrs.get(ATTR_CODE);

				currentBytecode = Integer.parseInt(bciAttr);
				int code = Integer.parseInt(codeAttr);
				callAttrs.clear();

				// we found a LogCompilation bc tag
				// e.g. "<bc code='182' bci='2'/>"
				// Now check in the current class bytecode
				// that the instruction at offset bci
				// has the same opcode as attribute code
				// if not then this is probably a TieredCompilation
				// context change. (TieredCompilation does not use
				// nested parse tags so have to use this heuristic
				// to check if we are still in the same method.

				if (DEBUG_LOGGING_BYTECODE)
				{
					logger.debug("BC Tag {} {}", currentBytecode, code);
				}

				currentInstruction = getInstructionAtIndex(instructions, currentBytecode);

				if (DEBUG_LOGGING_BYTECODE)
				{
					logger.debug("Instruction at {} is {}", currentBytecode, currentInstruction);
				}

				inMethod = false;

				if (currentInstruction != null)
				{
					int opcodeValue = currentInstruction.getOpcode().getValue();

					if (opcodeValue == code)
					{
						inMethod = true;
					}
				}
			}
				break;
			case TAG_CALL:
			{
				callAttrs.clear();
				callAttrs.putAll(tagAttrs);
			}
				break;
			case TAG_METHOD:
			{
				methodAttrs.clear();
				methodAttrs.putAll(tagAttrs);

				String nameAttr = methodAttrs.get(ATTR_NAME);

				inMethod = false;

				if (nameAttr != null && currentInstruction != null && currentInstruction.hasComment())
				{
					String comment = currentInstruction.getComment();

					inMethod = comment.contains(nameAttr);
				}
			}
				break;
			case TAG_INLINE_SUCCESS:
			{

				if (inMethod || isC2)
				{
					if (!sanityCheckInline(currentInstruction))
					{
						throw new AnnotationException("Expected an invoke instruction (in INLINE_SUCCESS)", currentBytecode,
								currentInstruction);
					}

					String reason = tagAttrs.get(ATTR_REASON);
					String annotationText = InlineUtil.buildInlineAnnotationText(true, reason, callAttrs, methodAttrs);

					result.put(currentBytecode, new LineAnnotation(annotationText, Color.GREEN));
				}
			}
				break;
			case TAG_INLINE_FAIL:
			{
				if (inMethod || isC2)
				{
					if (!sanityCheckInline(currentInstruction))
					{
						throw new AnnotationException("Expected an invoke instruction (in INLINE_FAIL)", currentBytecode,
								currentInstruction);
					}

					String reason = tagAttrs.get(ATTR_REASON);
					String annotationText = InlineUtil.buildInlineAnnotationText(false, reason, callAttrs, methodAttrs);

					result.put(currentBytecode, new LineAnnotation(annotationText, Color.RED));
				}
			}
				break;
			case TAG_BRANCH:
			{
				if (!result.containsKey(currentBytecode))
				{
					if (inMethod || isC2)
					{
						if (!sanityCheckBranch(currentInstruction))
						{
							throw new AnnotationException("Expected a branch instruction (in BRANCH)", currentBytecode,
									currentInstruction);
						}

						String branchAnnotation = buildBranchAnnotation(tagAttrs);

						result.put(currentBytecode, new LineAnnotation(branchAnnotation, Color.BLUE));
					}
				}
			}
				break;
			case TAG_INTRINSIC:
			{
				if (inMethod || isC2)
				{
					if (!sanityCheckIntrinsic(currentInstruction))
					{
						for (BytecodeInstruction ins : instructions)
						{
							logger.info("! instruction: {}", ins);
						}

						throw new AnnotationException("Expected an invoke instruction (IN INTRINSIC)", currentBytecode,
								currentInstruction);
					}

					StringBuilder reason = new StringBuilder();
					reason.append("Intrinsic: ").append(tagAttrs.get(ATTR_ID));

					result.put(currentBytecode, new LineAnnotation(reason.toString(), Color.GREEN));
				}
			}
				break;

			default:
				break;
			}
		}
	}

	private static String buildBranchAnnotation(Map<String, String> tagAttrs)
	{
		String count = tagAttrs.get(ATTR_BRANCH_COUNT);
		String taken = tagAttrs.get(ATTR_BRANCH_TAKEN);
		String notTaken = tagAttrs.get(ATTR_BRANCH_NOT_TAKEN);
		String prob = tagAttrs.get(ATTR_BRANCH_PROB);

		StringBuilder reason = new StringBuilder();

		if (count != null)
		{
			reason.append("Count: ").append(count).append(C_NEWLINE);
		}

		reason.append("Branch taken: ").append(taken).append(C_NEWLINE).append("Branch not taken: ").append(notTaken);

		if (prob != null)
		{
			reason.append(C_NEWLINE).append("Taken Probability: ").append(prob);
		}

		return reason.toString();
	}

	public static Task getLastTask(Journal journal)
	{
		// find the latest task tag
		// this is the most recent compile task for the member
		Task lastTask = null;

		for (Tag tag : journal.getEntryList())
		{
			if (tag instanceof Task)
			{
				lastTask = (Task) tag;
			}
		}

		return lastTask;
	}

	// used to detect if class file matches log file
	// only these bytecode instruction should be at the offset
	// for an inlining log statement
	public static boolean sanityCheckInline(BytecodeInstruction instr)
	{
		return sanityCheckInvoke(instr);
	}

	public static boolean sanityCheckIntrinsic(BytecodeInstruction instr)
	{
		return sanityCheckInvoke(instr);
	}

	private static boolean sanityCheckInvoke(BytecodeInstruction instr)
	{
		boolean sane = false;

		if (instr != null)
		{
			sane = instr.isInvoke();
		}

		return sane;
	}

	public static boolean sanityCheckBranch(BytecodeInstruction instr)
	{
		boolean sane = false;

		if (instr != null)
		{
			sane = instr.getOpcode().getMnemonic().startsWith("if");
		}

		return sane;
	}

	public static CompilerName getLastTaskCompiler(Journal journal)
	{
		Task lastTask = getLastTask(journal);

		CompilerName compilerName = null;

		if (lastTask != null)
		{
			compilerName = lastTask.getCompiler();
		}

		return compilerName;
	}

	public static Tag getParsePhase(Journal journal)
	{
		Tag parsePhase = null;

		Task lastTask = getLastTask(journal);

		if (lastTask != null)
		{
			CompilerName compilerName = lastTask.getCompiler();

			String parseAttributeName = ATTR_PARSE;

			if (compilerName == CompilerName.C1)
			{
				parseAttributeName = ATTR_BUILDIR;
			}

			List<Tag> parsePhases = lastTask.getNamedChildrenWithAttribute(TAG_PHASE, ATTR_NAME, parseAttributeName);

			int count = parsePhases.size();

			if (count != 1)
			{
				logger.warn("Unexpected parse phase count: {}", count);
			}
			else
			{
				parsePhase = parsePhases.get(0);
			}
		}

		return parsePhase;
	}
}