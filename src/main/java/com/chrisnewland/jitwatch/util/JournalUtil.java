/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.paint.Color;

import com.chrisnewland.jitwatch.model.CompilerName;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.LineAnnotation;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.model.Task;
import com.chrisnewland.jitwatch.model.bytecode.Instruction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JournalUtil
{
    private static final Logger logger = LoggerFactory.getLogger(JournalUtil.class);
    public static final boolean LETS_ASSUME_ITS_NOT_C2 = false;

    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */

    private JournalUtil() {
    }

	public static Map<Integer, LineAnnotation> buildBytecodeAnnotations(Journal journal, List<Instruction> instructions)
	{
		Map<Integer, LineAnnotation> result = new HashMap<>();

		if (journal != null)
		{
			CompilerName compilerName = getLastTaskCompiler(journal);

			Tag parsePhase = getParsePhase(journal);

			//TODO fix for JDK8
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

    private static void buildParseTagAnnotations(Tag parseTag, Map<Integer, LineAnnotation> result, List<Instruction> instructions,
			CompilerName compilerName)
	{
		List<Tag> children = parseTag.getChildren();

		int currentBytecode = -1;

		Map<String, String> methodAttrs = new HashMap<>();
		Map<String, String> callAttrs = new HashMap<>();

        boolean isC2 = isTheFormatC2(compilerName, LETS_ASSUME_ITS_NOT_C2);

        boolean inMethod = true;
		Instruction currentInstruction = null;

		for (Tag child : children)
		{
			String name = child.getName();
			Map<String, String> tagAttrs = child.getAttrs();

			switch (name)
			{
			case TAG_BC:
			{
                Parse_TAG_BC parse_TAG_BC = new Parse_TAG_BC(instructions, callAttrs, tagAttrs).invoke();
                inMethod = parse_TAG_BC.isInMethod();
                currentInstruction = parse_TAG_BC.getCurrentInstruction();
                currentBytecode = parse_TAG_BC.getCurrentBytecode();
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
                inMethod = parse_TAG_METHOD(methodAttrs, currentInstruction, tagAttrs);

			}
				break;
			case TAG_INLINE_SUCCESS:
			{
                parse_TAG_INLINE_SUCCESS(result, currentBytecode, methodAttrs, callAttrs, isC2, inMethod, tagAttrs);
			}
				break;
			case TAG_INLINE_FAIL:
			{
                parse_TAG_INLINE_FAIL(result, currentBytecode, methodAttrs, callAttrs, isC2, inMethod, tagAttrs);
			}
				break;
			case TAG_BRANCH:
			{
                parse_TAG_BRANCH(result, currentBytecode, isC2, inMethod, tagAttrs);
			}
				break;
			case TAG_INTRINSIC:
			{
                parse_TAG_INTRINSIC(result, currentBytecode, isC2, inMethod, tagAttrs);
			}
				break;

            default:
                break;
			}
		}
	}

    private static boolean isTheFormatC2(CompilerName compilerName, boolean isC2) {
        if (compilerName == CompilerName.C2)
        {
            isC2 = true;
        }
        return isC2;
    }

    private static void parse_TAG_INTRINSIC(Map<Integer, LineAnnotation> result, int currentBytecode, boolean isC2, boolean inMethod, Map<String, String> tagAttrs) {
        StringBuilder reason = new StringBuilder();
        reason.append("Intrinsic: ").append(tagAttrs.get(ATTR_ID));

        if (inMethod || isC2)
        {
            result.put(currentBytecode, new LineAnnotation(reason.toString(), Color.GREEN));
        }
    }

    private static void parse_TAG_BRANCH(Map<Integer, LineAnnotation> result, int currentBytecode, boolean isC2, boolean inMethod, Map<String, String> tagAttrs) {
        String count = tagAttrs.get(ATTR_BRANCH_COUNT);
        String taken = tagAttrs.get(ATTR_BRANCH_TAKEN);
        String notTaken = tagAttrs.get(ATTR_BRANCH_NOT_TAKEN);
        String prob = tagAttrs.get(ATTR_BRANCH_PROB);

        StringBuilder reason = new StringBuilder();

        if (count != null)
        {
            reason.append("Count: ").append(count).append("\n");
        }

        reason.append("Branch taken: ").append(taken).append("\nBranch not taken: ").append(notTaken);

        if (prob != null)
        {
            reason.append("\nProbability: ").append(prob);
        }

        if (!result.containsKey(currentBytecode))
        {
            if (inMethod || isC2)
            {
                result.put(currentBytecode, new LineAnnotation(reason.toString(), Color.BLUE));
            }
        }
    }

    private static void parse_TAG_INLINE_FAIL(Map<Integer, LineAnnotation> result, int currentBytecode, Map<String, String> methodAttrs, Map<String, String> callAttrs, boolean isC2, boolean inMethod, Map<String, String> tagAttrs) {
        String reason = tagAttrs.get(ATTR_REASON);
        String annotationText = InlineUtil.buildInlineAnnotationText(false, reason, callAttrs, methodAttrs);
        if (inMethod || isC2)
        {
            result.put(currentBytecode, new LineAnnotation(annotationText, Color.RED));
        }
    }

    private static void parse_TAG_INLINE_SUCCESS(Map<Integer, LineAnnotation> result, int currentBytecode, Map<String, String> methodAttrs, Map<String, String> callAttrs, boolean isC2, boolean inMethod, Map<String, String> tagAttrs) {
        String reason = tagAttrs.get(ATTR_REASON);
        String annotationText = InlineUtil.buildInlineAnnotationText(true, reason, callAttrs, methodAttrs);
        if (inMethod || isC2)
        {
            result.put(currentBytecode, new LineAnnotation(annotationText, Color.GREEN));
        }
    }

    private static boolean parse_TAG_METHOD(Map<String, String> methodAttrs, Instruction currentInstruction, Map<String, String> tagAttrs) {
        boolean inMethod;
        methodAttrs.clear();
        methodAttrs.putAll(tagAttrs);

        String nameAttr = methodAttrs.get(ATTR_NAME);

        inMethod = false;

        if (nameAttr != null && currentInstruction != null && currentInstruction.hasComment())
        {
            String comment = currentInstruction.getComment();

            inMethod = comment.contains(nameAttr);
        }
        return inMethod;
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
                logger.info("Unexpected parse phase count: {}", count);
			}
			else
			{
				parsePhase = parsePhases.get(0);
			}
		}

		return parsePhase;
	}

    private static class Parse_TAG_BC {
        private List<Instruction> instructions;
        private Map<String, String> callAttrs;
        private Map<String, String> tagAttrs;
        private int currentBytecode;
        private boolean inMethod;
        private Instruction currentInstruction;

        public Parse_TAG_BC(List<Instruction> instructions, Map<String, String> callAttrs, Map<String, String> tagAttrs) {
            this.instructions = instructions;
            this.callAttrs = callAttrs;
            this.tagAttrs = tagAttrs;
        }

        private static Instruction getInstructionAtIndex(List<Instruction> instructions, int index)
        {
            Instruction found = null;

            for (Instruction instruction : instructions)
            {
                if (instruction.getOffset() == index)
                {
                    found = instruction;
                    break;
                }
            }

            return found;
        }

        public int getCurrentBytecode() {
            return currentBytecode;
        }

        public boolean isInMethod() {
            return inMethod;
        }

        public Instruction getCurrentInstruction() {
            return currentInstruction;
        }

        public Parse_TAG_BC invoke() {
            String bciAttr = tagAttrs.get(ATTR_BCI);
            String codeAttr = tagAttrs.get(ATTR_CODE);

            currentBytecode = Integer.parseInt(bciAttr);
            int code = Integer.parseInt(codeAttr);
            callAttrs.clear();

            currentInstruction = getInstructionAtIndex(instructions, currentBytecode);

            inMethod = false;

            if (currentInstruction != null)
            {
                int opcodeValue = currentInstruction.getOpcode().getValue();

                if (opcodeValue == code)
                {
                    inMethod = true;
                }
            }
            return this;
        }
    }
}
