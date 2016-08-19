/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.optimizedvcall;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_OVC;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyBlock;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.SourceMapper;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizedVirtualCallFinder
{
	// optimized virtual_call info output by
	// hotspot/src/share/tools/hsdis/vm/code/nmethod.cpp

	private static final Logger logger = LoggerFactory.getLogger(OptimizedVirtualCallFinder.class);

	private IReadOnlyJITDataModel model;
	private List<String> classLocations = new ArrayList<>();

	public OptimizedVirtualCallFinder(IReadOnlyJITDataModel model, List<String> classLocations)
	{
		this.model = model;
		this.classLocations = classLocations;
	}

	public OptimizedVirtualCall findOptimizedCall(AssemblyInstruction instruction)
	{
		if (DEBUG_LOGGING_OVC)
		{
			logger.debug("findOptimizedCall: {}", instruction);
		}

		OptimizedVirtualCall result = null;

		if (instruction != null && instruction.isOptimizedVCall())
		{
			if (DEBUG_LOGGING_OVC)
			{
				logger.debug("Instruction is an OVC");
			}

			VirtualCallSite callSite = instruction.getOptimizedVirtualCallSiteOrNull();

			if (DEBUG_LOGGING_OVC)
			{
				logger.debug("Found callSite: {}", callSite);
			}

			result = getOptimizedVirtualCall(callSite);
		}
		else
		{
			if (DEBUG_LOGGING_OVC)
			{
				logger.debug("Instruction is not an OVC");
			}
		}

		return result;
	}

	public OptimizedVirtualCall getOptimizedVirtualCall(VirtualCallSite callSite)
	{
		OptimizedVirtualCall result = null;

		if (callSite != null)
		{
			BytecodeInstruction bytecodeInstruction = null;

			MemberBytecode memberBytecode = getMemberBytecodeForCallSite(callSite);

			if (DEBUG_LOGGING_OVC)
			{
				logger.debug("VCS: {} found MemberBytecode {}", callSite, memberBytecode != null);
			}

			if (memberBytecode != null)
			{
				IMetaMember callerMember = findMember(memberBytecode.getMemberSignatureParts());

				if (DEBUG_LOGGING_OVC)
				{
					logger.debug("Found member for msp:\n{}\nMember:{}", memberBytecode.getMemberSignatureParts(), callerMember);
				}

				bytecodeInstruction = memberBytecode.getBytecodeAtOffset(callSite.getBytecodeOffset());

				if (DEBUG_LOGGING_OVC)
				{
					logger.debug("Found BytecodeInstruction: {}", bytecodeInstruction);
				}

				if (bytecodeInstruction != null)
				{
					IMetaMember calleeMember = null;

					try
					{
						calleeMember = ParseUtil.getMemberFromBytecodeComment(model, callerMember, bytecodeInstruction);
					}
					catch (LogParseException e)
					{
						logger.error("Could not get member from bytecode comment", e);
					}

					if (DEBUG_LOGGING_OVC)
					{
						logger.debug("=========================");
						logger.debug("callerMember: {}", callerMember);
						logger.debug("calleeMember: {}", calleeMember);
						logger.debug("callSite    : {}", callSite);
						logger.debug("bytecodeInstruction : {}", bytecodeInstruction);
						logger.debug("=========================");
					}

					if (callerMember != null && calleeMember != null)
					{
						result = new OptimizedVirtualCall(callerMember, calleeMember, callSite, bytecodeInstruction);
					}
					else
					{
						logger.error("Could not create OVC from\ncaller: {}\ncallee: {}\nCallSite was: {}", callerMember,
								calleeMember, callSite);
					}
				}
				else
				{
					logger.error("Could not find BytecodeInstruction for VCS: {}", callSite);
				}
			}
		}
		else
		{
			if (DEBUG_LOGGING_OVC)
			{
				logger.warn("Could not find memberBytecode for VCS: {}", callSite);
			}
		}

		return result;
	}

	public IMetaMember findMember(MemberSignatureParts msp)
	{
		IMetaMember result = null;

		String metaClassName = msp.getFullyQualifiedClassName();

		MetaClass metaClass = model.getPackageManager().getMetaClass(metaClassName);

		if (DEBUG_LOGGING_OVC)
		{
			logger.debug("Looking for metaClass: {} found: {}", metaClassName, metaClass);
		}

		if (metaClass != null)
		{
			result = metaClass.getMemberForSignature(msp);
		}

		return result;
	}

	private MemberBytecode getMemberBytecodeForCallSite(VirtualCallSite callSite)
	{
		if (DEBUG_LOGGING_OVC)
		{
			logger.debug("getMemberBytecodeForCallSite({})", callSite);
		}

		MemberBytecode result = null;

		if (callSite != null)
		{
			String callerClass = callSite.getClassName();

			MetaClass metaClass = model.getPackageManager().getMetaClass(callerClass);

			if (DEBUG_LOGGING_OVC)
			{
				logger.debug("Found MetaClass {} for callerClass {}", metaClass, callerClass);
			}

			if (metaClass != null)
			{
				ClassBC classBC = metaClass.getClassBytecode(model, classLocations);

				if (DEBUG_LOGGING_OVC)
				{
					logger.debug("Got ClassBC: {}", classBC != null);
				}

				if (classBC != null)
				{
					result = SourceMapper.getMemberBytecodeForSourceLine(classBC, callSite.getSourceLine());
				}
			}
		}

		if (DEBUG_LOGGING_OVC)
		{
			logger.debug("Got MemberBytecode: {}", result != null);
		}
		return result;
	}

	public List<OptimizedVirtualCall> findOptimizedCalls(IMetaMember member)
	{
		if (DEBUG_LOGGING_OVC)
		{
			logger.debug("Looking for OVCs for member: {}", member);
		}

		Set<OptimizedVirtualCall> squashDuplicatesSet = new HashSet<>();

		List<Compilation> compilations = member.getCompilations();
		
		int compilationCount = compilations.size();
		
		AssemblyMethod asmMethod = null;
		
		if (compilationCount > 0)
		{
			asmMethod = compilations.get(compilationCount - 1).getAssembly();
		}
		
		if (DEBUG_LOGGING_OVC)
		{
			logger.debug("Member assembly\n{}", asmMethod);
		}

		if (asmMethod != null)
		{
			for (AssemblyBlock block : asmMethod.getBlocks())
			{
				squashDuplicatesSet.addAll(findInstructionsForBlock(member, block));
			}
		}

		List<OptimizedVirtualCall> result = new ArrayList<>(squashDuplicatesSet);

		return result;
	}

	public List<OptimizedVirtualCall> findInstructionsForBlock(IMetaMember member, AssemblyBlock block)
	{
		List<OptimizedVirtualCall> result = new ArrayList<>();

		for (AssemblyInstruction instruction : block.getInstructions())
		{
			OptimizedVirtualCall optimizedVCall = findOptimizedCall(instruction);

			if (optimizedVCall != null && !result.contains(optimizedVCall))
			{
				if (DEBUG_LOGGING_OVC)
				{
					logger.debug("Found OVC {} for member {}", optimizedVCall, member);
				}

				result.add(optimizedVCall);
			}
		}

		return result;
	}
}