/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BCI;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BRANCH_COUNT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BRANCH_NOT_TAKEN;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BRANCH_PROB;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BRANCH_TAKEN;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_CODE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_REASON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_BYTECODE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BRANCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ELIMINATE_ALLOCATION;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_JVMS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.paint.Color;

import org.adoptopenjdk.jitwatch.journal.IJournalVisitable;
import org.adoptopenjdk.jitwatch.journal.JournalUtil;
import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.CompilerName;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.LineAnnotation;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.util.InlineUtil;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BytecodeAnnotationBuilder implements IJournalVisitable
{
	private static final Logger logger = LoggerFactory.getLogger(BytecodeAnnotationBuilder.class);

	private IMetaMember member;

	private List<BytecodeInstruction> instructions;

	private Map<Integer, LineAnnotation> result = new HashMap<>();

	public Map<Integer, LineAnnotation> buildBytecodeAnnotations(final IMetaMember member,
			final List<BytecodeInstruction> instructions) throws AnnotationException
	{
		this.member = member;
		this.instructions = instructions;
		result.clear();

		if (!member.isCompiled())
		{
			return result;
		}

		try
		{
			JournalUtil.visitParseTagsOfLastTask(member, this);

			JournalUtil.visitOptimizerTagsOfLastTask(member, this);

		}
		catch (LogParseException e)
		{
			logger.error("Error building bytecode annotations", e);

			Throwable cause = e.getCause();

			if (cause instanceof AnnotationException)
			{
				throw (AnnotationException) cause;
			}
		}

		return result;
	}

	@Override
	public void visitTag(Tag tag, IParseDictionary parseDictionary) throws LogParseException
	{
		switch (tag.getName())
		{
		case TAG_PARSE:
			if (JournalUtil.memberMatchesParseTag(member, tag, parseDictionary))
			{
				try
				{
					final CompilerName compilerName = JournalUtil.getCompilerNameForLastTask(member.getJournal());

					buildParseTagAnnotations(tag, result, instructions, compilerName);
				}
				catch (Exception e)
				{
					throw new LogParseException("Could not parse annotations", e);
				}
			}

			break;

		// <eliminate_allocation type='817'>
		// <jvms bci='44' method='818'/>
		// </eliminate_allocation>

		case TAG_ELIMINATE_ALLOCATION:

			List<Tag> childrenJVMS = tag.getNamedChildren(TAG_JVMS);

			for (Tag tagJVMS : childrenJVMS)
			{
				String bci = tagJVMS.getAttribute(ATTR_BCI);

				if (bci != null)
				{
					try
					{
						int bciValue = Integer.parseInt(bci);

						BytecodeInstruction instr = getInstructionAtIndex(instructions, bciValue);

						if (instr != null)
						{
							StringBuilder builder = new StringBuilder();
							builder.append("Object does not escape method.\n");
							builder.append("Heap allocation has been eliminated.\n");

							String typeID = tag.getAttribute(ATTR_TYPE);

							if (typeID != null)
							{
								String typeOrKlassName = ParseUtil.lookupType(typeID, parseDictionary);

								if (typeOrKlassName != null)
								{
									builder.append("Eliminated allocation was of type ").append(typeOrKlassName);
								}
							}

							storeAnnotation(bciValue, new LineAnnotation(builder.toString(), Color.GRAY), result);
							instr.setEliminated(true);
						}
					}
					catch (NumberFormatException nfe)
					{
						logger.error("Couldn't parse BCI", nfe);
					}
				}

			}

			break;
		}
	}

	private void buildParseTagAnnotations(Tag parseTag, Map<Integer, LineAnnotation> result,
			List<BytecodeInstruction> instructions, CompilerName compilerName) throws AnnotationException
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("Building parse tag annotations");
		}

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

			if (DEBUG_LOGGING)
			{
				logger.debug("Examining child tag {}", child);
			}

			switch (name)
			{
			case TAG_BC:
			{
				String bciAttr = tagAttrs.get(ATTR_BCI);
				String codeAttr = tagAttrs.get(ATTR_CODE);

				currentBytecode = Integer.parseInt(bciAttr);
				int code = Integer.parseInt(codeAttr);
				callAttrs.clear();

				// TODO fix this old logic

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

					storeAnnotation(currentBytecode, new LineAnnotation(annotationText, Color.GREEN), result);
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

					storeAnnotation(currentBytecode, new LineAnnotation(annotationText, Color.RED), result);
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

						storeAnnotation(currentBytecode, new LineAnnotation(branchAnnotation, Color.BLUE), result);
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

					storeAnnotation(currentBytecode, new LineAnnotation(reason.toString(), Color.GREEN), result);
				}
			}
				break;

			default:
				break;
			}
		}
	}

	private void storeAnnotation(int bci, LineAnnotation annotation, Map<Integer, LineAnnotation> result)
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("BCI: {} Anno: {}", bci, annotation.getAnnotation());
		}

		result.put(bci, annotation);
	}

	private String buildBranchAnnotation(Map<String, String> tagAttrs)
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

	private BytecodeInstruction getInstructionAtIndex(List<BytecodeInstruction> instructions, int index)
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
}