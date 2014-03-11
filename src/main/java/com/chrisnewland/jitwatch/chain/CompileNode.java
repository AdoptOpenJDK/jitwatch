package com.chrisnewland.jitwatch.chain;

import java.util.ArrayList;
import java.util.List;

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
}
