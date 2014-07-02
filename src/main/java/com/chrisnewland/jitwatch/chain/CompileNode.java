package com.chrisnewland.jitwatch.chain;

import java.util.ArrayList;
import java.util.List;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import com.chrisnewland.jitwatch.model.IMetaMember;

public class CompileNode
{
	private IMetaMember member;
	
	private boolean inlined = false;
	private String inlineReason = null;
	
	private List<CompileNode> children;
	
	private CompileNode parent = null;
	
	private String methodID = null;
	
	public CompileNode(IMetaMember member, String methodID)
	{
		this.member = member;
		this.methodID = methodID;
		
		children = new ArrayList<>();
	}
	
	public String getMethodID()
	{
		return methodID;
	}

	public IMetaMember getMember()
	{
		return member;
	}
	
	public void setInlined(boolean inlined, String reason)
	{
		this.inlined = inlined;
		this.inlineReason = reason;
	}
	
	public boolean isInlined()
	{
		return inlined;
	}
	
	public String getInlineReason()
	{
		return inlineReason;
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
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		show(this, builder, 0);

		return builder.toString();
	}

	private void show(CompileNode node, StringBuilder builder, int depth)
	{
		if (depth > 0)
		{
			for (int i = 0; i < depth; i++)
			{
				builder.append("\t");
			}

			builder.append(" -> ");

			builder.append(node.getMember().getMemberName());

			builder.append("[");

			if (node.getMember().isCompiled())
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
