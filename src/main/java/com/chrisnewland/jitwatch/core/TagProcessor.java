package com.chrisnewland.jitwatch.core;

import java.util.Map;

import com.chrisnewland.jitwatch.util.StringUtil;

public class TagProcessor
{
	// feed it lines until it completes a tag
	private Tag currentTag;

	public TagProcessor()
	{

	}

	public Tag processLine(String line)
	{
//		System.out.println("ct: " + currentTag);
//		System.out.println("@@: " + line);

		Tag result = null;

		if (line != null && line.length() > 3)
		{
			if (line.charAt(0) == '<')
			{
				// closing tag
				if (line.charAt(1) == '/')
				{
					String closeName = line.substring(2, line.length() - 1);

	//				System.out.println("Closing: " + closeName);

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
					int indexEndName = line.indexOf(' ');

					//System.out.println("idx: " + indexEndName);

					if (indexEndName == -1)
					{
						indexEndName = line.indexOf('>');
					}

					String name = line.substring(1, indexEndName);

					String remainder = line.substring(indexEndName);

					Map<String, String> attrs = StringUtil.getLineAttributes(remainder);

					Tag t = new Tag(name, attrs);

					if (currentTag == null)
					{
						currentTag = t;
		//				System.out.println("cur: " + currentTag);
					}
					else
					{
						currentTag.addChild(t);
		//				System.out.println("added child: " + t + " to " + currentTag);
					}

					if (line.charAt(line.length() - 2) == '/')
					{
						if (name.equals(currentTag.getName()))
						{
							// self closing
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
		}

		return result;
	}
}
