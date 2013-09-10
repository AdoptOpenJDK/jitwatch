package com.chrisnewland.jitwatch.model;

import java.util.List;
import java.util.Map;

public interface IMetaMember
{
	public static final String PUBLIC = "public";
	public static final String PRIVATE = "private";
	public static final String PROTECTED = "protected";
	public static final String STATIC = "static";
	public static final String FINAL = "final";
	public static final String SYNCHRONIZED = "synchronized";
	public static final String STRICTFP = "strictfp";
	public static final String NATIVE = "native";
	public static final String ABSTRACT = "abstract";

	public static final String[] MODIFIERS = new String[] { PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, SYNCHRONIZED, STRICTFP,
			NATIVE, ABSTRACT };

	public List<String> getQueuedAttributes();

	public MetaClass getMetaClass();

	public String getQueuedAttribute(String key);

	public List<String> getCompiledAttributes();

	public String getCompiledAttribute(String key);

	public void addCompiledAttribute(String key, String value);

	public void setQueuedAttributes(Map<String, String> queuedAttributes);

	public boolean isQueued();

	public void setCompiledAttributes(Map<String, String> compiledAttributes);

	public void addCompiledAttributes(Map<String, String> additionalAttrs);

	public boolean isCompiled();

	public String toStringUnqualifiedMethodName();

	public boolean matches(String input);

	public String getNativeCode();

	public void setNativeCode(String nativecode);
	
	public String getSignatureRegEx();
	
	public String getSignatureForBytecode();

}
