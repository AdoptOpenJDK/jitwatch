package org.adoptopenjdk.jitwatch.optimizedvcall;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyBlock;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizedVirtualCallFinder
{
	// optimized virtual_call info output by
	// hotspot/src/share/tools/hsdis/vm/code/nmethod.cpp

	private static final Logger logger = LoggerFactory.getLogger(OptimizedVirtualCallFinder.class);

	private static final Pattern PATTERN_ASSEMBLY_CALL_SIG = Pattern.compile("^; - (.*)::(.*)@(.*)\\s\\(line\\s(.*)\\)");

	public static OptimizedVirtualCall findOptimizedCall(IMetaMember callingMember, AssemblyInstruction instruction)
	{
		OptimizedVirtualCall result = null;

		List<String> commentLines = instruction.getCommentLines();

		int pos = 0;

		for (String line : commentLines)
		{
			if (line.contains(JITWatchConstants.S_OPTIMIZED_VIRTUAL_CALL))
			{
				if (pos >= 2)
				{
					String callerLine = commentLines.get(pos - 1);
					String calleeLine = commentLines.get(pos - 2);

					result = getOptimizedVirtualCall(callingMember, callerLine, calleeLine);
					break;
				}
			}

			pos++;
		}

		return result;
	}

	public static OptimizedVirtualCall getOptimizedVirtualCall(IMetaMember callingMember, String callerLine, String calleeLine)
	{
		VirtualCallSite caller = buildCallSiteForLine(callerLine);
		VirtualCallSite callee = buildCallSiteForLine(calleeLine);

		OptimizedVirtualCall result = null;

		if (caller != null && callee != null)
		{
			result = new OptimizedVirtualCall(callingMember, caller, callee);
		}

		return result;
	}

	public static VirtualCallSite buildCallSiteForLine(String line)
	{
		Matcher matcher = PATTERN_ASSEMBLY_CALL_SIG.matcher(line);

		VirtualCallSite result = null;

		if (matcher.find())
		{
			String className = matcher.group(1);
			String methodName = matcher.group(2);
			String bytecodeOffset = matcher.group(3);
			String lineNumber = matcher.group(4);

			try
			{
				result = new VirtualCallSite(className, methodName, Integer.parseInt(bytecodeOffset), Integer.parseInt(lineNumber));
			}
			catch (NumberFormatException nfe)
			{
				logger.warn("Could not parse CallSite from line: {}", line);
			}
		}

		return result;
	}

	public static List<OptimizedVirtualCall> findOptimizedCalls(IMetaMember member)
	{
		List<OptimizedVirtualCall> result = new ArrayList<>();

		AssemblyMethod asmMethod = member.getAssembly();

		if (asmMethod != null)
		{
			for (AssemblyBlock block : asmMethod.getBlocks())
			{
				for (AssemblyInstruction instruction : block.getInstructions())
				{
					OptimizedVirtualCall optimizedVCall = findOptimizedCall(member, instruction);

					if (optimizedVCall != null && !result.contains(optimizedVCall))
					{
						result.add(optimizedVCall);
					}
				}
			}
		}

		return result;
	}
}