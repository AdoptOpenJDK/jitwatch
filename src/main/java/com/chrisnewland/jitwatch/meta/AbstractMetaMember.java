package com.chrisnewland.jitwatch.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMetaMember implements IMetaMember
{
	protected MetaClass methodClass;
	protected String nativeCode = null;

	protected boolean isQueued = false;
	protected boolean isCompiled = false;

	protected Map<String, String> queuedAttributes = new ConcurrentHashMap<>();
	protected Map<String, String> compiledAttributes = new ConcurrentHashMap<>();

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
		String ts = toString();

		return makeUnqualified(ts);
	}

	protected String makeUnqualified(String sig)
	{
		int openParams = sig.lastIndexOf('(');

		if (openParams != -1)
		{
			int pos = openParams;

			int lastDot = -1;

			while (pos-- > 0)
			{
				if (sig.charAt(pos) == '.' && lastDot == -1)
				{
					lastDot = pos;
				}

				if (sig.charAt(pos) == ' ')
				{
					break;
				}
			}

			StringBuilder builder = new StringBuilder(sig);
			builder.delete(pos + 1, lastDot + 1);
			sig = builder.toString();

		}

		return sig;
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
		String unqualifiedSig = toStringUnqualifiedMethodName();

		return unqualifiedSig;
	}
}
