/*
 * Copyright (c) 2013-2017 Chris Newland.
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
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_PREALLOCATED;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_REASON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_BYTECODE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_PARSE_HIR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ASSERT_NULL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_BRANCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CAST_UP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_COMMENT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DEPENDENCY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_DIRECT_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ELIMINATE_ALLOCATION;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_ELIMINATE_LOCK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_HOT_THROW;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_JVMS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_LATE_INLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OBSERVE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PHASE_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PREDICTED_CALL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_UNCOMMON_TRAP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_VIRTUAL_CALL;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adoptopenjdk.jitwatch.compilation.AbstractCompilationVisitable;
import org.adoptopenjdk.jitwatch.compilation.CompilationUtil;
import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.BCIOpcodeMap;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.PackageManager;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.TooltipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BytecodeAnnotationBuilder extends AbstractCompilationVisitable
{
	private static final Logger logger = LoggerFactory.getLogger(BytecodeAnnotationBuilder.class);

	private IMetaMember currentMember;

	private IReadOnlyJITDataModel model;

	private BytecodeAnnotations bcAnnotations = new BytecodeAnnotations();

	private boolean verifyBytecodeInstructions;
	private boolean processAnnotationsForInlinedMethods;

	private EnumSet<BCAnnotationType> allowedAnnotations;

	private PackageManager packageManager;

	private Set<Tag> unhandledTags = new HashSet<>();

	public BytecodeAnnotationBuilder(boolean verifyBytecodeInstructions)
	{
		this(verifyBytecodeInstructions, false);
	}

	public BytecodeAnnotationBuilder(boolean verifyBytecodeInstructions, boolean processAnnotationsForInlinedMethods)
	{
		this.verifyBytecodeInstructions = verifyBytecodeInstructions;
		this.processAnnotationsForInlinedMethods = processAnnotationsForInlinedMethods;

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
		ignoreTags.add(TAG_ASSERT_NULL);
		ignoreTags.add(TAG_CAST_UP);
		ignoreTags.add(TAG_COMMENT);
	}

	public BytecodeAnnotations buildBytecodeAnnotations(final IMetaMember member, int compilationIndex, IReadOnlyJITDataModel model)
			throws AnnotationException
	{
		return buildBytecodeAnnotations(member, compilationIndex, model, EnumSet.allOf(BCAnnotationType.class));
	}

	public BytecodeAnnotations buildBytecodeAnnotations(final IMetaMember member, int compilationIndex, IReadOnlyJITDataModel model,
			EnumSet<BCAnnotationType> allowedAnnotations) throws AnnotationException
	{
		this.currentMember = member;
		this.model = model;
		this.allowedAnnotations = allowedAnnotations;

		this.packageManager = model.getPackageManager();

		bcAnnotations.clear();

		String vmVersion = model.getVmVersionRelease();

		if (member != null)
		{
			if (!member.isCompiled())
			{
				return bcAnnotations;
			}

			Compilation compilation = member.getCompilation(compilationIndex);

			try
			{
				buildParseTagAnnotations(vmVersion, compilation);

				buildEliminationTagAnnotations(vmVersion, compilation);
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

	private void buildParseTagAnnotations(String vmVersion, Compilation compilation) throws LogParseException
	{
		CompilationUtil.visitParseTagsOfCompilation(compilation, this);
	}

	private void buildEliminationTagAnnotations(String vmVersion, Compilation compilation) throws LogParseException
	{
		if (vmVersion != null && vmVersion.startsWith("1.9"))
		{
			CompilationUtil.visitEliminationTagsOfCompilation(compilation, this);
		}
		else
		{
			CompilationUtil.visitOptimizerTagsOfCompilation(compilation, this);
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

	private void visitTagParse(Tag tagParse, IParseDictionary parseDictionary) throws LogParseException
	{
		String methodID = tagParse.getAttributes().get(ATTR_METHOD);

		if (CompilationUtil.memberMatchesMethodID(currentMember, methodID, parseDictionary))
		{
			try
			{
				buildParseTagAnnotations(tagParse, bcAnnotations, parseDictionary);
			}
			catch (Exception e)
			{
				throw new LogParseException("Could not parse annotations", e);
			}
		}
		else
		{
			if (!isLateInline(tagParse))
			{
				logger.warn("Parse tag does not appear to be for member {}", currentMember.getMemberName());
				logger.warn("Method ID: {}\nTag was:\n{}", methodID, tagParse.toString(true));
				logger.warn("Dictionary:\n{}", parseDictionary.toString());
			}
		}
	}

	private boolean isLateInline(Tag parseTag)
	{
		boolean result = false;

		if (parseTag != null)
		{
			Tag parent = parseTag.getParent();

			if (parent != null)
			{
				String parseTagMethod = parseTag.getAttributes().get(ATTR_METHOD);

				List<Tag> lateInlines = parent.getNamedChildren(TAG_LATE_INLINE);

				for (Tag lateInline : lateInlines)
				{
					String method = lateInline.getAttributes().get(ATTR_METHOD);

					if (method != null && method.equals(parseTagMethod))
					{
						result = true;
						break;
					}
				}

			}
		}
		return result;
	}

	private void visitTagEliminateAllocation(Tag tagEliminateAllocation, IParseDictionary parseDictionary)
	{
		List<Tag> childrenJVMS = tagEliminateAllocation.getNamedChildren(TAG_JVMS);

		String typeID = tagEliminateAllocation.getAttributes().get(ATTR_TYPE);

		String typeOrKlassName = null;

		if (typeID != null)
		{
			typeOrKlassName = ParseUtil.lookupType(typeID, parseDictionary);

			if (typeOrKlassName != null)
			{
				for (Tag tagJVMS : childrenJVMS)
				{
					Map<String, String> tagJVMSAttributes = tagJVMS.getAttributes();

					String attrBCI = tagJVMSAttributes.get(ATTR_BCI);

					int bciValue = 0;

					if (attrBCI != null)
					{
						try
						{
							bciValue = Integer.parseInt(attrBCI);
						}
						catch (NumberFormatException nfe)
						{
							logger.error("Couldn't parse bci attribute {} tag {}", attrBCI, tagJVMS.toString(true));
							continue;
						}
					}
					else
					{
						logger.error("Missing bci attribute on tag {}", tagJVMS.toString(true));
					}

					String methodID = tagJVMSAttributes.get(ATTR_METHOD);

					BCIOpcodeMap bciOpcodeMap = parseDictionary.getBCIOpcodeMap(methodID);
					
					//logger.info("method {} {} {}", methodID, parseDictionary.getParseMethod(), bciOpcodeMap.entrySet());

					if (CompilationUtil.memberMatchesMethodID(currentMember, methodID, parseDictionary))
					{
						storeEliminatedAllocation(currentMember, bciValue, typeOrKlassName, bciOpcodeMap);
					}
					else if (processAnnotationsForInlinedMethods)
					{
						IMetaMember inlinedMember = findMemberForInlinedMethod(tagJVMS, parseDictionary);

						if (inlinedMember != null)
						{
							storeEliminatedAllocation(inlinedMember, bciValue, typeOrKlassName, bciOpcodeMap);
						}
						else
						{
							unhandledTags.add(tagJVMS);
						}
					}
				}
			}
			else
			{
				logger.error("Unknown type attribute {} on tag {}", typeID, tagEliminateAllocation.toString(true));
			}
		}
		else
		{
			logger.error("Missing type attribute on tag {}", tagEliminateAllocation.toString(true));
		}
	}

	private void storeEliminatedAllocation(IMetaMember member, int bciValue, String typeOrKlassName, BCIOpcodeMap bciOpcodeMap)
	{		
		Object referencedObject = null;

		MetaClass eliminatedMetaClass = packageManager.getMetaClass(typeOrKlassName);

		if (eliminatedMetaClass != null)
		{
			referencedObject = eliminatedMetaClass;
		}
		else
		{
			referencedObject = typeOrKlassName;
		}

		Opcode opcode = bciOpcodeMap.get(bciValue);
		
		boolean isInlined = (opcode != null && opcode.isInvoke());

		String annotation = buildEliminatedAllocationAnnotation(typeOrKlassName, isInlined);
		
		putAnnotation(member, bciValue, new LineAnnotation(annotation, BCAnnotationType.ELIMINATED_ALLOCATION, referencedObject));
	}

	private IMetaMember findMemberForInlinedMethod(Tag tagJVMS, IParseDictionary parseDictionary)
	{
		IMetaMember member = null;

		String methodID = tagJVMS.getAttributes().get(ATTR_METHOD);
		
		if (methodID != null)
		{
			member = ParseUtil.lookupMember(methodID, parseDictionary, model);
		}

		return member;
	}

	private void putAnnotation(IMetaMember member, int bci, LineAnnotation annotation)
	{
		if (allowedAnnotations.contains(annotation.getType()))
		{
			bcAnnotations.addAnnotation(member, bci, annotation);
		}
	}

	private void visitTagEliminateLock(Tag tagEliminateLock, IParseDictionary parseDictionary)
	{
		String kind = tagEliminateLock.getAttributes().get(ATTR_KIND);
		
		List<Tag> childrenJVMS = tagEliminateLock.getNamedChildren(TAG_JVMS);

		if (childrenJVMS.size() > 0)
		{
			for (Tag tagJVMS : childrenJVMS)
			{
				Map<String, String> tagJVMSAttributes = tagJVMS.getAttributes();

				String attrBCI = tagJVMSAttributes.get(ATTR_BCI);

				int bciValue = 0;

				if (attrBCI != null)
				{
					try
					{
						bciValue = Integer.parseInt(attrBCI);
					}
					catch (NumberFormatException nfe)
					{
						logger.error("Couldn't parse bci attribute {} tag {}", attrBCI, tagJVMS.toString(true));
						continue;
					}
				}
				else
				{
					logger.error("Missing bci attribute on tag {}", tagJVMS.toString(true));
				}

				String methodID = tagJVMSAttributes.get(ATTR_METHOD);

				BCIOpcodeMap bciOpcodeMap = parseDictionary.getBCIOpcodeMap(methodID);
				
				//logger.info("current {} methodID {} parseMethod {}", currentMember.toStringUnqualifiedMethodName(true, true), methodID, parseDictionary.getParseMethod());

				if (CompilationUtil.memberMatchesMethodID(currentMember, methodID, parseDictionary))
				{
					storeElidedLock(currentMember, bciValue, kind, bciOpcodeMap);
				}
				else if (processAnnotationsForInlinedMethods)
				{
					IMetaMember inlinedMember = findMemberForInlinedMethod(tagJVMS, parseDictionary);

					if (inlinedMember != null)
					{
						storeElidedLock(inlinedMember, bciValue, kind, bciOpcodeMap);
					}
					else
					{
						unhandledTags.add(tagJVMS);
					}
				}
				
			} // end for
		}
	}
	
	private void storeElidedLock(IMetaMember member, int bciValue, String kind, BCIOpcodeMap bciOpcodeMap)
	{		
		Opcode opcode = bciOpcodeMap.get(bciValue);
		
		boolean isInlined = (opcode != null && opcode.isInvoke());

		String annotation = buildElidedLockAnnotation(isInlined);
		
		putAnnotation(member, bciValue, new LineAnnotation(annotation, BCAnnotationType.LOCK_ELISION, kind));
	}
	
	private String buildElidedLockAnnotation(boolean isInlined)
	{
		StringBuilder builder = new StringBuilder();
		
		if (isInlined)
		{
			builder.append("A lock was eliminated due to inlining at this bci");
		}
		else
		{
			builder.append("A lock was eliminated at this bci");

		}
		
		return builder.toString();
	}

	private void visitTagUncommonTrap(Tag tag)
	{
		UncommonTrap trap = UncommonTrap.parse(tag);

		if (trap != null)
		{
			putAnnotation(currentMember, trap.getBCI(), new LineAnnotation(trap.toString(), BCAnnotationType.UNCOMMON_TRAP));
		}
	}

	private void buildParseTagAnnotations(Tag parseTag, BytecodeAnnotations annotations, IParseDictionary parseDictionary)
			throws AnnotationException
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

		String currentMethodID = parseTag.getAttributes().get(ATTR_METHOD);

		BytecodeInstruction currentInstruction = null;

		for (Tag child : children)
		{
			String name = child.getName();
			
			Map<String, String> tagAttrs = child.getAttributes();

			if (DEBUG_LOGGING_BYTECODE)
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

				IMetaMember inlinedMember = ParseUtil.lookupMember(lastMethodAttrs.get(ATTR_ID), parseDictionary, model);

				LineAnnotation lineAnnotation = new LineAnnotation(annotationText, BCAnnotationType.INLINE_SUCCESS, inlinedMember);
								
				putAnnotation(currentMember, currentBytecode, lineAnnotation);

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
				
				IMetaMember inlinedMember = ParseUtil.lookupMember(lastMethodAttrs.get(ATTR_ID), parseDictionary, model);

				LineAnnotation lineAnnotation = new LineAnnotation(annotationText, BCAnnotationType.INLINE_FAIL, inlinedMember);
								
				putAnnotation(currentMember, currentBytecode, lineAnnotation);

				break;
			}

			case TAG_BRANCH:
			{
				if (!sanityCheckBranch(currentInstruction))
				{
					throw new AnnotationException("Expected a branch instruction (BRANCH)", currentBytecode, currentInstruction);
				}

				String branchAnnotation = buildBranchAnnotation(tagAttrs);

				putAnnotation(currentMember, currentBytecode, new LineAnnotation(branchAnnotation, BCAnnotationType.BRANCH));
				
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

				putAnnotation(currentMember, currentBytecode,
						new LineAnnotation(reason.toString(), BCAnnotationType.INTRINSIC_USED));

				break;
			}

			case TAG_UNCOMMON_TRAP:
			{
				String trapMethod = child.getAttributes().get(ATTR_METHOD);

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

			case TAG_VIRTUAL_CALL:
				putAnnotation(currentMember, currentBytecode,
						new LineAnnotation("Virtual call, not inlined", BCAnnotationType.VIRTUAL_CALL));
				break;

			case TAG_HOT_THROW:
			{

				MemberBytecode memberBytecode = currentMember.getMemberBytecode();

				if (memberBytecode != null)
				{
					ExceptionTable exceptionTable = memberBytecode.getExceptionTable();

					if (exceptionTable != null)
					{
						ExceptionTableEntry entry = exceptionTable.getEntryForBCI(currentBytecode);

						if (entry != null)
						{
							int exceptionBCI = entry.getTarget();

							String exceptionType = entry.getType();

							String preallocated = child.getAttributes().get(ATTR_PREALLOCATED);

							StringBuilder reason = new StringBuilder();

							reason.append(exceptionType.replaceAll(S_SLASH, S_DOT)).append(" thrown by this operation");

							BCAnnotationType annotationType;

							if (preallocated != null && "1".equals(preallocated))
							{
								reason.append(" has been pre-allocated.");
								annotationType = BCAnnotationType.HOT_THROW_PREALLOCATED;
							}
							else
							{
								reason.append(" was not pre-allocated.");
								annotationType = BCAnnotationType.HOT_THROW_NOT_PREALLOCATED;
							}

							putAnnotation(currentMember, exceptionBCI, new LineAnnotation(reason.toString(), annotationType));
						}
					}
					else
					{
						logger.warn("No ExceptionTable found for {}", currentMember);

					}
				}
				else
				{
					logger.warn("No MemberBytecode found for {}", currentMember);
				}
				break;
			}

			default:
				handleOther(child);
				break;
			}
		}
	}

	private String buildInlineAnnotation(IParseDictionary parseDictionary, Map<String, String> methodAttrs,
			Map<String, String> callAttrs, String reason, boolean inlined)
	{
		return TooltipUtil.buildInlineAnnotationText(inlined, reason, callAttrs, methodAttrs, parseDictionary);
	}

	private String buildEliminatedAllocationAnnotation(String typeOrKlassName, boolean isInlined)
	{
		StringBuilder builder = new StringBuilder();
		
		if (isInlined)
		{
			builder.append("Heap allocation");

			if (typeOrKlassName != null)
			{
				builder.append(" of type ").append(typeOrKlassName);
			}
			
			builder.append(S_NEWLINE).append("was eliminated due to inlining at this bci");
		}
		else
		{
			builder.append("Object");
			
			if (typeOrKlassName != null)
			{
				builder.append(" of type ").append(typeOrKlassName);
			}
			
			builder.append(" does not escape method.\n");
			
			builder.append("Heap allocation has been eliminated.\n");
		}
		
		return builder.toString();
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
	public boolean sanityCheckInline(BytecodeInstruction instr)
	{
		return verifyBytecodeInstructions ? sanityCheckInvoke(instr) : true;
	}

	public boolean sanityCheckIntrinsic(BytecodeInstruction instr)
	{
		return verifyBytecodeInstructions ? sanityCheckInvoke(instr) : true;
	}

	private boolean sanityCheckInvoke(BytecodeInstruction instr)
	{
		boolean sane = false;

		if (instr != null)
		{
			sane = instr.getOpcode().isInvoke();
		}

		return sane;
	}

	public boolean sanityCheckBranch(BytecodeInstruction instr)
	{
		boolean sane = false;

		if (instr != null)
		{
			sane = instr.getOpcode().getMnemonic().startsWith("if");
		}

		return verifyBytecodeInstructions ? sane : true;
	}

	private BytecodeInstruction getInstructionAtIndex(int index)
	{
		BytecodeInstruction found = null;

		for (BytecodeInstruction instruction : currentMember.getInstructions())
		{
			if (instruction.getOffset() == index)
			{
				found = instruction;
				break;
			}
		}

		return found;
	}

	public Set<Tag> getUnhandledTags()
	{
		return unhandledTags;
	}
}