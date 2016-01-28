package org.adoptopenjdk.jitwatch.jarscan;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOUBLE_QUOTE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_STATIC_INIT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.List;

import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;

public class FreqInlineCounter implements IJarScanOperation
{
	private int freqInlineSize;
	
	private StringBuilder builder = new StringBuilder();

	public FreqInlineCounter(int freqInlineSize)
	{
		this.freqInlineSize = freqInlineSize;
	}

	public String getReport()
	{
		return builder.toString();
	}
	
	@Override
	public void processInstructions(String className, MemberBytecode memberBytecode)
	{		
		List<BytecodeInstruction> instructions = memberBytecode.getInstructions();

		if (instructions != null && instructions.size() > 0)
		{
			BytecodeInstruction lastInstruction = instructions.get(instructions.size() - 1);

			// final instruction is a return for 1 byte
			int bcSize = 1 + lastInstruction.getOffset();

			MemberSignatureParts msp = memberBytecode.getMemberSignatureParts();

			if (bcSize >= freqInlineSize && !S_STATIC_INIT.equals(msp.getMemberName()))
			{
				builder.append(C_DOUBLE_QUOTE);
				builder.append(className);
				builder.append(C_DOUBLE_QUOTE);
				builder.append(C_COMMA);

				builder.append(C_DOUBLE_QUOTE);
				builder.append(msp.getMemberName());
				builder.append(C_DOUBLE_QUOTE);
				builder.append(C_COMMA);

				builder.append(bcSize);
				builder.append(S_NEWLINE);
			}		
		}
	}
}
