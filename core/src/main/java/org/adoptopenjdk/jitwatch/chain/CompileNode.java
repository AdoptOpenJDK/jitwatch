/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.chain;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.util.ParseUtil;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class CompileNode
{	
	private boolean inlined = false;
	
	private boolean virtualCall = false;
	
	private String tooltip;
	
	private List<CompileNode> children;
	
	private CompileNode parent = null;
	
	private String methodID = null;
	
	private IParseDictionary parseDictionary;
	
	private IReadOnlyJITDataModel model;

	private Compilation compilation;
	
	public static CompileNode createRootNode(Compilation compilation, String methodID, IParseDictionary parseDictionary, IReadOnlyJITDataModel model)
	{
		CompileNode root = new CompileNode(methodID);
		root.compilation = compilation;
		root.parseDictionary = parseDictionary;
		root.model = model;
		
		return root;
	}
	
	public Compilation getCompilation()
	{
		return compilation;
	}

	public CompileNode(String methodID)
	{
		this.methodID = methodID;
		
		children = new ArrayList<>();
	}
	
	public String getMethodID()
	{
		return methodID;
	}

	public void setInlined(boolean inlined)
	{
		this.inlined = inlined;
	}
	
	public boolean isInlined()
	{
		return inlined;
	}
	
	public boolean isVirtualCall()
	{
		return virtualCall;
	}

	public void setVirtualCall(boolean virtualCall)
	{
		this.virtualCall = virtualCall;
	}

	public boolean isCompiled()
	{
		boolean result = false;
		
		IMetaMember member = getMember();
		
		if (member != null)
		{
			result = member.isCompiled();
		}
		
		return result;
	}
	
	public void addChild(CompileNode child)
	{
		child.parent = this;
		children.add(child);
	}
	
	public List<CompileNode> getChildren()
	{
		return children;
	}
	
	public CompileNode getParent()
	{
		return parent;
	}
	
	public void setTooltipText(String tooltip)
	{
		this.tooltip = tooltip;
	}
	
	
	public String getTooltipText()
	{
		return tooltip;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		show(this, builder, 0);

		return builder.toString();
	}
		
	private CompileNode getRoot()
	{
		if (parent == null)
		{
			return this;
		}
		else
		{
			return getParent().getRoot();
		}
	}
	
	public IMetaMember getMember()
	{
		CompileNode root = getRoot();
		
		return ParseUtil.lookupMember(methodID, root.parseDictionary, root.model);
	}
		
	public String getMemberName()
	{
		CompileNode root = getRoot();
		
		return ParseUtil.getMethodName(methodID, root.parseDictionary);
	}

	private void show(CompileNode node, StringBuilder builder, int depth)
	{
		if (depth >= 0)
		{
			for (int i = 0; i < depth; i++)
			{
				builder.append("\t");
			}

			builder.append(" -> ");

			builder.append(node.getMemberName());

			builder.append(" [");

			if (node.isCompiled())
			{
				builder.append("C");
			}

			if (node.isInlined())
			{
				builder.append("I");
			}

			builder.append("]");

			if (depth == 0)
			{
				builder.append(C_NEWLINE);
			}
		}

		for (CompileNode child : node.getChildren())
		{
			show(child, builder, depth + 1);
		}

		if (node.getChildren().size() == 0)
		{
			builder.append(C_NEWLINE);
		}
	}
}
