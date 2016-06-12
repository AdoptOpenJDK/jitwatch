/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ACTION;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BCI;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMMENT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_REASON;

import java.util.Map;

import org.adoptopenjdk.jitwatch.model.Tag;

public class UncommonTrap
{
	private int bci;
	private String reason;
	private String action;
	private String comment;

	public int getBCI()
	{
		return bci;
	}

	public String getReason()
	{
		return reason;
	}

	public String getAction()
	{
		return action;
	}

	public String getComment()
	{
		return comment;
	}

	public UncommonTrap(int bci, String reason, String action, String comment)
	{
		this.bci = bci;
		this.reason = reason;
		this.action = action;
		this.comment = comment;
	}

	public static UncommonTrap parse(Tag tag)
	{
		UncommonTrap trap = null;
		
		Map<String, String> tagAttributes = tag.getAttributes();

		String bci = tagAttributes.get(ATTR_BCI);
		String reason = tagAttributes.get(ATTR_REASON);
		String action = tagAttributes.get(ATTR_ACTION);
		String comment = tagAttributes.get(ATTR_COMMENT);

		if (bci != null)
		{
			int bciValue = Integer.valueOf(bci);

			trap = new UncommonTrap(bciValue, reason, action, comment);
		}

		return trap;

	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Uncommon trap");
		builder.append(" (");
		builder.append("reason:");
		builder.append(reason);
		builder.append(", action:");
		builder.append(action);

		if (comment != null)
		{
			builder.append(" comment:");
			builder.append(comment);
		}

		builder.append(")");

		return builder.toString();
	}

}
