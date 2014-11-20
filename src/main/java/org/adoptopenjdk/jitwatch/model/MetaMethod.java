/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_MEMBER_CREATION;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class MetaMethod extends AbstractMetaMember
{
    private String methodToString;

    public MetaMethod(Method method, MetaClass methodClass)
    {
        this.methodToString = method.toString();
        this.metaClass = methodClass;

        memberName = method.getName();
        returnType = method.getReturnType();
        paramTypes = Arrays.asList(method.getParameterTypes());

        // Can include non-method modifiers such as volatile so AND with
        // acceptable values
        modifier = method.getModifiers() & Modifier.methodModifiers();

        isVarArgs = method.isVarArgs();

        checkPolymorphicSignature(method);

        if (DEBUG_MEMBER_CREATION)
        {
        	logger.debug("Created MetaMethod: {}", toString());
        }
    }

    @Override
    public String toString()
    {
        String methodSigWithoutThrows = methodToString;

        int closingParentheses = methodSigWithoutThrows.indexOf(')');

        if (closingParentheses != methodSigWithoutThrows.length() - 1)
        {
            methodSigWithoutThrows = methodSigWithoutThrows.substring(0, closingParentheses + 1);
        }

        return methodSigWithoutThrows;
    }

    @Override
    public String getSignatureForBytecode()
    {
        String ts = methodToString;

        int openParams = ts.lastIndexOf('(');

        // make method name unqualified

        if (openParams != -1)
        {
            int pos = openParams;

            int lastDot = -1;

            while (pos-- > 0)
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
            if (lastDot != -1)
            {
                builder.delete(pos + 1, lastDot + 1);
            }
            ts = builder.toString();

        }

        return ts;
    }
}