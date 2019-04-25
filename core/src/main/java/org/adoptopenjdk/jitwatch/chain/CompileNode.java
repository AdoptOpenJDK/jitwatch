/*
 * Copyright (c) 2013-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

	private String reason;

	private String tooltip;

	private List<CompileNode> children;

	private CompileNode parent = null;

	private String methodID = null;

	private IParseDictionary parseDictionary;

	private IReadOnlyJITDataModel model;

	private Compilation compilation;

	private int callerBCI;

	public static CompileNode createRootNode(Compilation compilation, String methodID, IParseDictionary parseDictionary,
			IReadOnlyJITDataModel model)
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

	public int getCallerBCI()
	{
		return callerBCI;
	}

	public void setCallerBCI(int callerBCI)
	{
		this.callerBCI = callerBCI;
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

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}

	public void setTooltipText(String tooltip)
	{
		this.tooltip = tooltip;
	}

	public String getTooltipText()
	{
		return tooltip;
	}

	@Override public String toString()
	{
		StringBuilder builder = new StringBuilder();

		show(this, builder, 0);

		return builder.toString();
	}

	public CompileNode getRoot()
	{
		if (parent == null)
		{
			return this;
		}
		else
		{
			return parent.getRoot();
		}
	}

	public boolean isCompileRoot()
	{
		return parent == null;
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

		if (node.getChildren().isEmpty())
		{
			builder.append(C_NEWLINE);
		}
	}

	public String getCallerMember()
	{
		String result = null;

		if (parent != null)
		{
			result = parent.getMember().getFullyQualifiedMemberNameWithParamTypes();
		}

		return result;
	}

	public String getCalleeMember()
	{
		return getMember().getFullyQualifiedMemberNameWithParamTypes();
	}

	public String getCompilationRoot()
	{
		String result = null;

		CompileNode root = getRoot();

		if (root != null)
		{
			result = root.getMember().getFullyQualifiedMemberNameWithParamTypes();
		}

		return result;
	}

	public String getRootCompilationSignature()
	{
		String result = S_EMPTY;

		CompileNode root = getRoot();

		if (root != null)
		{
			Compilation compilation = root.getCompilation();

			if (compilation != null)
			{
				result = compilation.getSignature();
			}
		}

		return result;
	}

	/*
	CompileNode root contains Compilation
	make bidirectional?

	maybe calculate CompileNode root at time of Compilation
	means no need to recalculate when focus changes in triview

	chainA3 is a member with chainA1 as the root node and compilation

need to visit all compilations and create compilenode trees for each

every compilenode attaches to it's member's compilenode list

clean up use of CompileChainWalker to single pass

currently member has list of Compilation (where it is root node)

example of BigMethod
it is a compilenode (failed inline) under chainA4 (new info)
it is a compilenode root with compilation (already known)


	 */

	public String getRootCompilationCompiler()
	{
		String result = null;

		CompileNode root = getRoot();

		if (root != null)
		{
			Compilation compilation = root.getCompilation();

			if (compilation != null)
			{
				result = compilation.getCompiler();
			}
		}

		return result;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CompileNode that = (CompileNode) o;
		return callerBCI == that.callerBCI && Objects.equals(parent, that.parent) && Objects.equals(methodID, that.methodID);
	}

	@Override public int hashCode()
	{
		return Objects.hash(parent, methodID, callerBCI);
	}
}
