package com.chrisnewland.jitwatch.meta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetaMethod implements Comparable<MetaMethod>
{
    private Method method;
    private boolean isQueued = false;
    private boolean isCompiled = false;

    private MetaClass methodClass;
    
    private String nativeCode = null;
    
    private Map<String, String> queuedAttributes = new ConcurrentHashMap<>();
    private Map<String, String> compiledAttributes = new ConcurrentHashMap<>();

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

    public MetaMethod(Method method, MetaClass methodClass)
    {
        this.method = method;
        this.methodClass = methodClass;
    }

    public List<String> getQueuedAttributes()
    {
        List<String> attrList = new ArrayList<String>(queuedAttributes.keySet());
        Collections.sort(attrList);

        return attrList;
    }

    public MetaClass getMetaClass()
    {
        return methodClass;
    }

    public String getQueuedAttribute(String key)
    {
        return queuedAttributes.get(key);
    }

    public List<String> getCompiledAttributes()
    {
        List<String> attrList = new ArrayList<String>(compiledAttributes.keySet());
        Collections.sort(attrList);

        return attrList;
    }

    public String getCompiledAttribute(String key)
    {
        return compiledAttributes.get(key);
    }
    
    public void addCompiledAttribute(String key, String value)
    {
        compiledAttributes.put(key,value);
    }

    @Override
    public String toString()
    {
        String methodSigWithoutThrows = method.toString();

        int closingParentheses = methodSigWithoutThrows.indexOf(')');

        if (closingParentheses != methodSigWithoutThrows.length() - 1)
        {
            methodSigWithoutThrows = methodSigWithoutThrows.substring(0, closingParentheses + 1);
        }

        return methodSigWithoutThrows;
    }

    public void setQueuedAttributes(Map<String, String> queuedAttributes)
    {
        isQueued = true;
        this.queuedAttributes = queuedAttributes;
    }

    public boolean isQueued()
    {
        return isQueued;
    }

    public void setCompiledAttributes(Map<String, String> compiledAttributes)
    {
        isCompiled = true;
        isQueued = false;
        this.compiledAttributes = compiledAttributes;
    }
    
    public void addCompiledAttributes(Map<String, String> additionalAttrs)
    {
    	compiledAttributes.putAll(additionalAttrs);
    }

    public boolean isCompiled()
    {
        return isCompiled;
    }

    public String toStringUnqualifiedMethodName()
    {
        String ts = toString();

        return makeUnqualified(ts);
    }

    private String makeUnqualified(String sig)
    {
        int openParams = sig.lastIndexOf('(');

        if (openParams != -1)
        {
            int pos = openParams;

            int lastDot = -1;

            while (pos-- >= 0)
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

    public String getSignatureRegEx()
    {
        String unqualifiedSig = makeUnqualified(method.toString());


        return unqualifiedSig;
    }

    public String getSignatureForBytecode()
    {
        String ts = method.toString();

        int openParams = ts.lastIndexOf('(');

        if (openParams != -1)
        {
            int pos = openParams;

            int lastDot = -1;

            while (pos-- >= 0)
            {
                if (ts.charAt(pos) == '.' && lastDot == -1)
                {
                    lastDot = pos;
                }

                if (ts.charAt(pos) == ' ')
                {
                    break;
                }
            }

            StringBuilder builder = new StringBuilder(ts);
            builder.delete(pos + 1, lastDot + 1);
            ts = builder.toString();

        }

        return ts;
    }

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
    
    public String getNativeCode()
    {
        return nativeCode;
    }

    public void setNativeCode(String nativecode)
    {
        this.nativeCode = nativecode;
    }

    @Override
    public int compareTo(MetaMethod o)
    {
        if (o == null)
        {
            return -1;
        }
        else
        {
            return toString().compareTo(o.toString());
        }
    }
}