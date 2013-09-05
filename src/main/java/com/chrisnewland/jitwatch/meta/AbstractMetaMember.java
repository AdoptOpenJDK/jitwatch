package com.chrisnewland.jitwatch.meta;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.chrisnewland.jitwatch.core.StringUtil;

public abstract class AbstractMetaMember implements IMetaMember
{
	protected MetaClass methodClass;
	protected String nativeCode = null;

	protected boolean isQueued = false;
	protected boolean isCompiled = false;

	protected Map<String, String> queuedAttributes = new ConcurrentHashMap<>();
	protected Map<String, String> compiledAttributes = new ConcurrentHashMap<>();

	protected int modifier; // bitset
	protected String memberName;
	protected Class<?> returnType;
	protected Class<?>[] paramTypes;

	@Override
	public List<String> getQueuedAttributes()
	{
		List<String> attrList = new ArrayList<String>(queuedAttributes.keySet());
		Collections.sort(attrList);

		return attrList;
	}

	@Override
	public MetaClass getMetaClass()
	{
		return methodClass;
	}

	@Override
	public String getQueuedAttribute(String key)
	{
		return queuedAttributes.get(key);
	}

	@Override
	public List<String> getCompiledAttributes()
	{
		List<String> attrList = new ArrayList<String>(compiledAttributes.keySet());
		Collections.sort(attrList);

		return attrList;
	}

	@Override
	public String getCompiledAttribute(String key)
	{
		return compiledAttributes.get(key);
	}

	@Override
	public void addCompiledAttribute(String key, String value)
	{
		compiledAttributes.put(key, value);
	}

	@Override
	public void setQueuedAttributes(Map<String, String> queuedAttributes)
	{
		isQueued = true;
		this.queuedAttributes = queuedAttributes;
	}

	@Override
	public boolean isQueued()
	{
		return isQueued;
	}

	@Override
	public void setCompiledAttributes(Map<String, String> compiledAttributes)
	{
		isCompiled = true;
		isQueued = false;
		this.compiledAttributes = compiledAttributes;
	}

	@Override
	public void addCompiledAttributes(Map<String, String> additionalAttrs)
	{
		compiledAttributes.putAll(additionalAttrs);
	}

	@Override
	public boolean isCompiled()
	{
		return isCompiled;
	}

	@Override
	public String toStringUnqualifiedMethodName()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(Modifier.toString(modifier)).append(' ');

		if (returnType != null)
		{
			builder.append(returnType.getName()).append(' ');
		}

		builder.append(memberName);
		builder.append('(');

		if (paramTypes.length > 0)
		{
			for (Class<?> paramClass : paramTypes)
			{
				builder.append(paramClass.getName()).append(',');
			}

			builder.deleteCharAt(builder.length() - 1);
		}

		builder.append(')');

		return builder.toString();
	}

	@Override
	public boolean matches(String input)
	{
		// strip access mode and modifiers
		String nameToMatch = this.toString();

		for (String mod : MODIFIERS)
		{
			nameToMatch = nameToMatch.replace(mod + " ", "");
		}

		return nameToMatch.equals(input);
	}

	@Override
	public String getNativeCode()
	{
		return nativeCode;
	}

	@Override
	public void setNativeCode(String nativecode)
	{
		this.nativeCode = nativecode;
	}

	@Override
	public String getSignatureRegEx()
	{

		String anyChars = "(.*)";
		String spaceZeroOrMore = "( )*";
		String spaceOneOrMore = "( )+";
		String paramName = "([0-9a-zA-Z_]+)";
		String regexPackage = "([0-9a-zA-Z_\\.]*)";

		StringBuilder builder = new StringBuilder();

		builder.append("^");
		builder.append(anyChars);

		String modifiers = Modifier.toString(modifier);

		if (modifiers.length() > 0)
		{
			builder.append(modifiers).append(' ');
		}

		if (returnType != null)
		{
			String rt = returnType.getName();

			if (rt.contains("."))
			{
				rt = regexPackage + StringUtil.makeUnqualified(rt);
			}

			builder.append(rt);
			builder.append(' ');
		}

		if (this instanceof MetaConstructor)
		{
			builder.append(regexPackage);
			builder.append(StringUtil.makeUnqualified(memberName));
		}
		else
		{
			builder.append(memberName);
		}

		builder.append("\\(");

		if (paramTypes.length > 0)
		{
			for (Class<?> paramClass : paramTypes)
			{
				builder.append(spaceZeroOrMore);

				String paramType = paramClass.getName();

				if (paramType.contains("."))
				{
					paramType = regexPackage + StringUtil.makeUnqualified(paramType);
				}

				builder.append(paramType);
				builder.append(spaceOneOrMore);
				builder.append(paramName);
				builder.append(",");
			}

			builder.deleteCharAt(builder.length() - 1);
		}

		builder.append(spaceZeroOrMore);
		builder.append("\\)");
		builder.append(anyChars);
		builder.append("$");

		return builder.toString();
	}
}
