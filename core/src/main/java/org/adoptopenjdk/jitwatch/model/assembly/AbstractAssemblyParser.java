package org.adoptopenjdk.jitwatch.model.assembly;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_ASSEMBLY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.NATIVE_CODE_ENTRY_POINT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ENTITY_APOS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HEX_POSTFIX;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HEX_PREFIX;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_SQUARE_BRACKET;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_QUOTE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SEMICOLON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAssemblyParser implements IAssemblyParser
{
	protected static final Logger logger = LoggerFactory.getLogger(AbstractAssemblyParser.class);

	protected Architecture architecture;

	public AbstractAssemblyParser(Architecture architecture)
	{
		this.architecture = architecture;
	}
	
	public Architecture getArchitecture()
	{
		return architecture;
	}

	// TODO this is too much work
	// save the string blocks and parse on demand
	public AssemblyMethod parseAssembly(final String assemblyString)
	{
		final AssemblyLabels labels = new AssemblyLabels();

		String[] lines = assemblyString.split(S_NEWLINE);

		StringBuilder headerBuilder = new StringBuilder();

		AssemblyBlock currentBlock = new AssemblyBlock();
		currentBlock.setTitle(NATIVE_CODE_ENTRY_POINT);

		AssemblyInstruction lastInstruction = null;

		String lastLine = null;

		AssemblyMethod method = null;

		for (int i = 0; i < lines.length; i++)
		{
			if (DEBUG_LOGGING_ASSEMBLY)
			{
				logger.debug("line: '{}'", lines[i]);
			}

			if (i == 0)
			{
				method = new AssemblyMethod(lines[i], architecture);
			}

			String line = lines[i].replace(S_ENTITY_APOS, S_QUOTE);
			line = line.replaceFirst("^ +", "");

			if (line.startsWith(S_HASH))
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Assembly header: {}", line);
				}

				headerBuilder.append(line).append(S_NEWLINE);
			}
			else if (line.startsWith(S_OPEN_SQUARE_BRACKET))
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("new AssemblyBlock: {}", line);
				}

				method.addBlock(currentBlock);
				currentBlock = new AssemblyBlock();
				currentBlock.setTitle(line);
			}
			else if (line.startsWith(S_SEMICOLON))
			{
				if (DEBUG_LOGGING_ASSEMBLY)
				{
					logger.debug("Extended comment? '{}'", line);
				}

				if (lastInstruction != null)
				{
					lastInstruction.addCommentLine(line);
				}
			}
			else
			{
				AssemblyInstruction instr = createInstruction(labels, line);

				if (instr == null && lastLine.trim().startsWith(S_HASH) && !line.startsWith(S_HEX_PREFIX)
						&& !line.contains(' ' + S_HEX_PREFIX))
				{
					// remove last newline
					headerBuilder.setLength(headerBuilder.length() - S_NEWLINE.length());

					headerBuilder.append(line).append(S_NEWLINE);

					// update untrimmedLine since it is used to update
					// lastUntrimmedLine at end of loop
					line = lastLine + line;
				}
				else if (instr == null && lastLine.trim().startsWith(S_SEMICOLON) && lastInstruction != null)
				{
					lastInstruction.appendToLastCommentLine(line);

					// update untrimmedLine since it is used to update
					// lastUntrimmedLine at end of loop
					line = lastLine + line;
				}
				else
				{
					boolean replaceLast = false;
					if (instr == null && i < lines.length - 1)
					{
						// try appending current and next lines together
						String nextUntrimmedLine = lines[i + 1].replace(S_ENTITY_APOS, S_QUOTE);

						instr = createInstruction(labels, line + nextUntrimmedLine);

						if (instr != null)
						{
							i++;
						}
					}

					if (instr == null && lastInstruction != null)
					{
						// try appending last and current lines together
						instr = createInstruction(labels, lastLine + line);
						if (instr != null)
						{
							replaceLast = true;
						}
					}

					if (instr != null)
					{
						if (replaceLast)
						{
							currentBlock.replaceLastInstruction(instr);
						}
						else
						{
							currentBlock.addInstruction(instr);
						}

						lastInstruction = instr;
					}
					else
					{
						logger.error("Could not parse assembly: {}", line);
					}
				}
			}
			lastLine = line;
		}

		method.addBlock(currentBlock);

		method.setHeader(headerBuilder.toString());

		labels.buildLabels();

		return method;
	}

	@Override
	public boolean isAddress(String mnemonic, String operand)
	{
		return (operand.startsWith(S_HEX_PREFIX) || operand.endsWith(S_HEX_POSTFIX)) && isJump(mnemonic);
	}
}
