/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.suggestion;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IParseDictionary;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.PackageManager;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.model.Task;
import com.chrisnewland.jitwatch.util.JournalUtil;

public class AttributeSuggestionWalker extends AbstractSuggestionVisitable
{
	private IParseDictionary parseDictionary;

	public AttributeSuggestionWalker(IReadOnlyJITDataModel model)
	{
		super(model);
	}

	@Override
	public void visit(IMetaMember mm)
	{
		// check journal for inlined member calls

		if (mm.isCompiled())
		{
			Journal journal = JournalUtil.getJournal(model, mm);

			if (journal != null)
			{
				Task lastTaskTag = JournalUtil.getLastTask(journal);

				if (lastTaskTag != null)
				{
					parseDictionary = lastTaskTag.getParseDictionary();

					List<Tag> parseTags = JournalUtil.getParseTags(journal);

					for (Tag parseTag : parseTags)
					{
						processParseTag(parseTag);
					}
				}
			}
		}
	}

	private void processParseTag(Tag parseTag)
	{
		String methodId = null;

		for (Tag child : parseTag.getChildren())
		{
			String tagName = child.getName();
			Map<String, String> attrs = child.getAttrs();

			switch (tagName)
			{
			case TAG_METHOD:
			{
				methodId = attrs.get(ATTR_ID);
			}
				break;

			case TAG_CALL:
			{
				methodId = attrs.get(ATTR_METHOD);
			}
				break;

			case TAG_INLINE_FAIL:
			{
				StringBuilder reason = new StringBuilder(attrs.get(ATTR_REASON));

				Tag methodTag = parseDictionary.getMethod(methodId);

				String methodName = methodTag.getAttrs().get(ATTR_NAME);

				String klassId = methodTag.getAttrs().get(ATTR_HOLDER);

				Tag klassTag = parseDictionary.getKlass(klassId);

				String metaClassName = klassTag.getAttrs().get(ATTR_NAME);

				String returnTypeId = methodTag.getAttrs().get(ATTR_RETURN);

				String argumentsTypeId = methodTag.getAttrs().get(ATTR_ARGUMENTS);

				String returnType = null;
				String[] argumentTypes = null;

				if (returnTypeId != null)
				{
					Tag returnTypeTag = parseDictionary.getType(returnTypeId);
					returnType = returnTypeTag.getAttrs().get(ATTR_NAME);
				}

				if (argumentsTypeId != null)
				{
					String[] typeIDs = argumentsTypeId.split(S_SPACE);

					argumentTypes = new String[typeIDs.length];

					int pos = 0;

					for (String typeID : typeIDs)
					{
						Tag typeTag = parseDictionary.getType(typeID);
						typeIDs[pos++] = typeTag.getAttrs().get(ATTR_NAME);
					}
				}

				String methodBytecodes = methodTag.getAttrs().get(ATTR_BYTES);
				String invocationCount = methodTag.getAttrs().get(ATTR_IICOUNT);
				
				reason.append(" invocation count: ").append(invocationCount);
				reason.append(" bytecodes: ").append(methodBytecodes).append(".");

				PackageManager pm = model.getPackageManager();

				MetaClass metaClass = pm.getMetaClass(metaClassName);

				IMetaMember member = metaClass.getMemberFromSignature(methodName, returnType, argumentTypes);

				Suggestion suggestion = new Suggestion(member, reason.toString(), Integer.parseInt(methodBytecodes));

				suggestionList.add(suggestion);
			}

				break;
			case TAG_PARSE:
			{
				// recurse
				processParseTag(child);
			}
				break;
			}
		}
	}

}