/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_MEMBER_CREATION;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class MetaMethod extends AbstractMetaMember
{
    private String methodToString;

    public MetaMethod(Method method, MetaClass methodClass)
    {
    	super(method.getName());
    	
        this.methodToString = method.toString();
        this.metaClass = methodClass;

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

    public void setParamTypes(List<Class<?>> types)
    {
    	this.paramTypes = types;
    }
    
    public void setReturnType(Class<?> returnType)
    {
    	this.returnType = returnType;
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
}