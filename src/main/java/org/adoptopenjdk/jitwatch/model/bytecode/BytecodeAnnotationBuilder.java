/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BRANCH_COUNT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BRANCH_NOT_TAKEN;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BRANCH_PROB;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BRANCH_TAKEN;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_CODE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_REASON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BCI;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_BYTECODE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_PARSE_HIR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BRANCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DIRECT_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ELIMINATE_ALLOCATION;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ELIMINATE_LOCK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_JVMS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_UNCOMMON_TRAP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DEPENDENCY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PREDICTED_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OBSERVE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.journal.AbstractJournalVisitable;
import org.adoptopenjdk.jitwatch.journal.JournalUtil;
import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.adoptopenjdk.jitwatch.util.TooltipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BytecodeAnnotationBuilder extends AbstractJournalVisitable
{
	private static final Logger logger = LoggerFactory.getLogger(BytecodeAnnotationBuilder.class);

	private IMetaMember member;

	private IReadOnlyJITDataModel model;

	private BytecodeAnnotations bcAnnotations = new BytecodeAnnotations();

	public BytecodeAnnotationBuilder()
	{
		ignoreTags.add(TAG_KLASS);
		ignoreTags.add(TAG_TYPE);
		ignoreTags.add(TAG_DEPENDENCY);
		ignoreTags.add(TAG_PHASE);
		ignoreTags.add(TAG_PARSE_DONE);
		ignoreTags.add(TAG_DIRECT_CALL);
		ignoreTags.add(TAG_PARSE);
		ignoreTags.add(TAG_PHASE_DONE);
		ignoreTags.add(TAG_PREDICTED_CALL);
		ignoreTags.add(TAG_OBSERVE);
	}

	public BytecodeAnnotations buildBytecodeAnnotations(final IMetaMember member, IReadOnlyJITDataModel model)
			throws AnnotationException
	{
		this.member = member;
		this.model = model;

		bcAnnotations.clear();

		String vmVersion = model.getVmVersionRelease();

		if (member != null)
		{
			if (!member.isCompiled())
			{
				return bcAnnotations;
			}

			try
			{
				buildParseTagAnnotations(vmVersion, member.getJournal());

				buildEliminationTagAnnotations(vmVersion, member.getJournal());
			}
			catch (LogParseException e)
			{
				logger.error("Error building bytecode annotations", e);

				Throwable cause = e.getCause();

				if (cause != null)
				{
					logger.error("Cause", cause);

					if (cause instanceof AnnotationException)
					{
						throw (AnnotationException) cause;
					}
				}
			}
		}
		return bcAnnotations;
	}

	private void buildParseTagAnnotations(String vmVersion, Journal journal) throws LogParseException
	{
		JournalUtil.visitParseTagsOfLastTask(member.getJournal(), this);
	}

	private void buildEliminationTagAnnotations(String vmVersion, Journal journal) throws LogParseException
	{
		if (vmVersion != null && vmVersion.startsWith("1.9"))
		{
			JournalUtil.visitEliminationTagsOfLastTask(member.getJournal(), this);
		}
		else
		{
			JournalUtil.visitOptimizerTagsOfLastTask(member.getJournal(), this);
		}
	}

	@Override
	public void visitTag(Tag tag, IParseDictionary parseDictionary) throws LogParseException
	{	
		switch (tag.getName())
		{
		case TAG_PARSE:
			visitTagParse(tag, parseDictionary);
			break;

		case TAG_ELIMINATE_ALLOCATION:
			visitTagEliminateAllocation(tag, parseDictionary);
			break;

		case TAG_ELIMINATE_LOCK:
			visitTagEliminateLock(tag, parseDictionary);
			break;

		default:
			handleOther(tag);
			break;
		}
	}

	private void visitTagParse(Tag tag, IParseDictionary parseDictionary) throws LogParseException
	{
		String methodID = tag.getAttribute(ATTR_METHOD);
		
		if (JournalUtil.memberMatchesMethodID(member, methodID, parseDictionary))
		{
			try
			{
				buildParseTagAnnotations(tag, bcAnnotations, parseDictionary);
			}
			catch (Exception e)
			{
				throw new LogParseException("Could not parse annotations", e);
			}
		}
		else
		{
			logger.warn("Parse tag does not appear to be for member {}", member.getFullyQualifiedMemberName());
		}
	}

	private void visitTagEliminateAllocation(Tag tag, IParseDictionary parseDictionary)
	{
		List<Tag> childrenJVMS = tag.getNamedChildren(TAG_JVMS);

		for (Tag tagJVMS : childrenJVMS)
		{
			String methodID = tagJVMS.getAttribute(ATTR_METHOD);

			if (JournalUtil.memberMatchesMethodID(member, methodID, parseDictionary))
			{
				String bci = tagJVMS.getAttribute(ATTR_BCI);

				if (bci != null)
				{
					try
					{
						int bciValue = Integer.parseInt(bci);

						BytecodeInstruction instr = getInstructionAtIndex(bciValue);

						if (instr != null)
						{
							String typeID = tag.getAttribute(ATTR_TYPE);

							String typeOrKlassName = null;

							if (typeID != null)
							{
								typeOrKlassName = ParseUtil.lookupType(typeID, parseDictionary);

							}

							String annotation = buildEliminatedAllocationAnnotation(typeOrKlassName);

							if (instr.getOpcode() == Opcode.NEW)
							{
								bcAnnotations.addAnnotation(bciValue,
										new LineAnnotation(annotation, BCAnnotationType.ELIMINATED_ALLOCATION));

								instr.setEliminated(true);
							}
							else
							{
								logger.warn("Found heap elimination on instruction that is not Opcode.NEW: {} @ {} ({}/{})",
										instr.getOpcode(), instr.getOffset(), typeID, typeOrKlassName);
							}
						}
					}
					catch (NumberFormatException nfe)
					{
						logger.error("Couldn't parse BCI", nfe);
					}
				}
				else
				{
					logger.info("Elimination not for member");
				}
			}

		}
	}

	// <eliminate_lock lock='0'>
	// </eliminate_lock>
	// <eliminate_lock lock='1'>
	// <jvms bci='-1' method='823'/>
	// <jvms bci='21' method='818'/>
	// </eliminate_lock>

	// JDK9 has much more detail in eliminate_lock
	// (callnode.cpp:log_lock_optimization)
	private void visitTagEliminateLock(Tag tag, IParseDictionary parseDictionary)
	{
		List<Tag> childrenJVMS = tag.getNamedChildren(TAG_JVMS);

		if (childrenJVMS.size() > 0)
		{
			StringBuilder builder = new StringBuilder();

			builder.append("A lock has been eliminated").append(S_NEWLINE);
			builder.append("Call chain:").append(S_NEWLINE);

			int depth = 0;

			for (Tag tagJVMS : childrenJVMS)
			{
				String methodID = tagJVMS.getAttribute(ATTR_METHOD);

				if (JournalUtil.memberMatchesMethodID(member, methodID, parseDictionary))
				{
					String bci = tagJVMS.getAttribute(ATTR_BCI);

					if (bci != null)
					{
						try
						{
							int bciValue = Integer.parseInt(bci);

							if (methodID != null)
							{
								IMetaMember member = ParseUtil.lookupMember(methodID, parseDictionary, model);

								if (member != null)
								{
									if (bciValue != -1)
									{
										builder.append(StringUtil.repeat(C_SPACE, depth * 2)).append("->").append(C_SPACE);
										depth++;
									}

									builder.append(member.toStringUnqualifiedMethodName(true));
								}
							}

							builder.append(S_NEWLINE);

							if (bciValue != -1)
							{
								bcAnnotations.addAnnotation(bciValue,
										new LineAnnotation(builder.toString().trim(), BCAnnotationType.LOCK_ELISION));

								BytecodeInstruction instr = getInstructionAtIndex(bciValue);

								if (instr != null && instr.isLock())
								{
									instr.setEliminated(true);
								}
							}
						}
						catch (NumberFormatException nfe)
						{
							logger.error("Couldn't parse BCI", nfe);
						}
					}
				}
				else
				{
					logger.info("Elimination not for member");
				}
			}
		}
	}

	private void visitTagUncommonTrap(Tag tag)
	{
		UncommonTrap trap = UncommonTrap.parse(tag);

		if (trap != null)
		{
			bcAnnotations.addAnnotation(trap.getBCI(), new LineAnnotation(trap.toString(), BCAnnotationType.UNCOMMON_TRAP));
		}
	}

	private void buildParseTagAnnotations(Tag parseTag, BytecodeAnnotations annotations, IParseDictionary parseDictionary) throws AnnotationException
	{
		// Only interested in annotating the current method so
		// do not recurse into method or parse tags
		
		if (DEBUG_LOGGING)
		{
			logger.debug("Building parse tag annotations");
		}

		List<Tag> children = parseTag.getChildren();

		int currentBytecode = -1;

		Map<String, Map<String, String>> methodAttrs = new HashMap<>();
		Map<String, String> callAttrs = new HashMap<>();
		Map<String, String> lastMethodAttrs = new HashMap<>();

		String currentMethodID = parseTag.getAttribute(ATTR_METHOD);

		BytecodeInstruction currentInstruction = null;

		for (Tag child : children)
		{
			String name = child.getName();
			Map<String, String> tagAttrs = child.getAttributes();

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

				if (DEBUG_LOGGING_BYTECODE)
				{
					logger.debug("BC Tag {} {}", currentBytecode, code);
				}

				currentInstruction = getInstructionAtIndex(currentBytecode);

				if (DEBUG_LOGGING_BYTECODE)
				{
					logger.debug("Instruction at {} is {}", currentBytecode, currentInstruction);
				}

				break;
			}

			case TAG_CALL:
			{
				callAttrs.clear();
				callAttrs.putAll(tagAttrs);

				lastMethodAttrs.clear();
				String calleeId = tagAttrs.get("method");
				if (calleeId != null)
				{
					Map<String, String> calleeAttrs = methodAttrs.get(calleeId);
					if (calleeAttrs != null)
					{
						lastMethodAttrs.putAll(calleeAttrs);
					}
				}

				break;
			}

			case TAG_METHOD:
			{
				String methodId = tagAttrs.get("id");
				if (methodId != null)
				{
					methodAttrs.put(methodId, tagAttrs);
				}
				break;
			}

			case TAG_INLINE_SUCCESS:
			{
				if (!sanityCheckInline(currentInstruction))
				{
					throw new AnnotationException("Expected an invoke instruction (in INLINE_SUCCESS)", currentBytecode,
							currentInstruction);
				}

				String reason = tagAttrs.get(ATTR_REASON);
				String annotationText = buildInlineAnnotation(parseDictionary, lastMethodAttrs, callAttrs, reason, true);

				bcAnnotations.addAnnotation(currentBytecode, new LineAnnotation(annotationText, BCAnnotationType.INLINE_SUCCESS));

				break;
			}

			case TAG_INLINE_FAIL:
			{

				if (!sanityCheckInline(currentInstruction))
				{
					throw new AnnotationException("Expected an invoke instruction (in INLINE_FAIL)", currentBytecode,
							currentInstruction);
				}

				String reason = tagAttrs.get(ATTR_REASON);
				String annotationText = buildInlineAnnotation(parseDictionary, lastMethodAttrs, callAttrs, reason, false);

				bcAnnotations.addAnnotation(currentBytecode, new LineAnnotation(annotationText, BCAnnotationType.INLINE_FAIL));

				break;
			}

			case TAG_BRANCH:
			{
				if (!bcAnnotations.hasAnnotationsForBCI(currentBytecode))
				{

					if (!sanityCheckBranch(currentInstruction))
					{
						throw new AnnotationException("Expected a branch instruction (BRANCH)", currentBytecode,
								currentInstruction);
					}

					String branchAnnotation = buildBranchAnnotation(tagAttrs);

					bcAnnotations.addAnnotation(currentBytecode, new LineAnnotation(branchAnnotation, BCAnnotationType.BRANCH));
				}

				break;
			}

			case TAG_INTRINSIC:
			{

				if (!sanityCheckIntrinsic(currentInstruction))
				{
					throw new AnnotationException("Expected an invoke instruction (INTRINSIC)", currentBytecode,
							currentInstruction);
				}

				StringBuilder reason = new StringBuilder();
				reason.append("Intrinsic: ").append(tagAttrs.get(ATTR_ID));

				bcAnnotations.addAnnotation(currentBytecode,
						new LineAnnotation(reason.toString(), BCAnnotationType.INTRINSIC_USED));

				break;
			}

			case TAG_UNCOMMON_TRAP:
			{
				String trapMethod = child.getAttribute(ATTR_METHOD);

				if (trapMethod == null || currentMethodID.equals(trapMethod))
				{
					visitTagUncommonTrap(child);
				}

				break;
			}

			case TAG_PHASE:
			{
				String phaseName = tagAttrs.get(ATTR_NAME);

				if (S_PARSE_HIR.equals(phaseName))
				{
					buildParseTagAnnotations(child, annotations, parseDictionary);
				}
				else
				{
					logger.warn("Don't know how to handle phase {}", phaseName);
				}

				break;
			}

			default:
				handleOther(child);
				break;
			}
		}
	}

	protected String buildInlineAnnotation(IParseDictionary parseDictionary, Map<String, String> methodAttrs,
																				 Map<String, String> callAttrs, String reason, boolean inlined)
	{
		return TooltipUtil.buildInlineAnnotationText(inlined, reason, callAttrs, methodAttrs,
				parseDictionary);
	}

	protected String buildEliminatedAllocationAnnotation(String typeOrKlassName)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Object does not escape method.\n");
		builder.append("Heap allocation has been eliminated.\n");

		if (typeOrKlassName != null)
		{
			builder.append("Eliminated allocation was of type ").append(typeOrKlassName);
		}
		return builder.toString();
	}

	protected String buildBranchAnnotation(Map<String, String> tagAttrs)
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

	private BytecodeInstruction getInstructionAtIndex(int index)
	{
		BytecodeInstruction found = null;

		for (BytecodeInstruction instruction : member.getInstructions())
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
