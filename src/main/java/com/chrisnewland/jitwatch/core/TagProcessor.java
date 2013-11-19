package com.chrisnewland.jitwatch.core;

import java.util.Map;

import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.model.Task;
import com.chrisnewland.jitwatch.util.StringUtil;

public class TagProcessor
{
	// feed it lines until it completes a tag
	private Tag currentTag;
	private Tag topTag = null;

	private static final char SLASH = '/';
	private static final char SPACE = ' ';
	private static final char OPEN_BRACKET = '<';
	private static final char CLOSE_BRACKET = '>';

	public TagProcessor()
	{

	}

	public Tag processLine(String line)
	{
		Tag result = null;

		if (line != null && line.length() > 3)
		{
			if (line.charAt(0) == OPEN_BRACKET)
			{
				// closing tag
				if (line.charAt(1) == SLASH)
				{
					String closeName = line.substring(2, line.length() - 1);

					if (closeName.equals(currentTag.getName()))
					{
						if (currentTag.getParent() == null)
						{
							result = currentTag;
						}
						else
						{
							currentTag = currentTag.getParent();
						}
					}
				}
				else
				{
					int indexEndName = line.indexOf(SPACE);

					if (indexEndName == -1)
					{
						indexEndName = line.indexOf(CLOSE_BRACKET);
					}

					String name = line.substring(1, indexEndName);

					String remainder = line.substring(indexEndName);

					Map<String, String> attrs = StringUtil.getLineAttributes(remainder);

					boolean selfClosing = (line.charAt(line.length() - 2) == SLASH);

					Tag t;

					if (JITWatchConstants.TAG_TASK.equals(name))
					{
						t = new Task(name, attrs, selfClosing);
					}
					else
					{
						t = new Tag(name, attrs, selfClosing);
					}

					if (currentTag == null)
					{
						// new tag at top level
						currentTag = t;
						topTag = t;
					}
					else
					{
						currentTag.addChild(t);
					}

					if (topTag instanceof Task)
					{
						switch (name)
						{
						case JITWatchConstants.TAG_TYPE:
							((Task) topTag).getParseDictionary().setType(attrs.get(JITWatchConstants.ATTR_ID), t);
							break;

						case JITWatchConstants.TAG_METHOD:
							((Task) topTag).getParseDictionary().setMethod(attrs.get(JITWatchConstants.ATTR_ID), t);
							break;

						case JITWatchConstants.TAG_KLASS:
							((Task) topTag).getParseDictionary().setKlass(attrs.get(JITWatchConstants.ATTR_ID), t);
							break;
						}
					}

					if (selfClosing)
					{
						if (name.equals(currentTag.getName()))
						{
							if (currentTag.getParent() == null)
							{
								result = currentTag;
							}
						}
					}
					else
					{
						// not closed
						currentTag = t;
					}
				}
			}
		}

		if (result != null)
		{
			currentTag = null;
			topTag = null;
		}

		return result;
	}

}