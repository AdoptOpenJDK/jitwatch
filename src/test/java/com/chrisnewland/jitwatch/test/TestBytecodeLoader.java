/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.test;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import org.junit.Test;

import com.chrisnewland.jitwatch.loader.BytecodeLoader;
import com.chrisnewland.jitwatch.model.MetaMethod;
import com.chrisnewland.jitwatch.util.ClassUtil;

public class TestBytecodeLoader
{
	private Method getMethod(String fqClassName, String method, Class<?>[] paramTypes)
	{
		Method m = null;

		try
		{
			Class<?> clazz = ClassUtil.loadClassWithoutInitialising(fqClassName);
			m = clazz.getDeclaredMethod(method, paramTypes);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return m;
	}

	@Test
	public void testBytecodeSignature()
	{
	    String className = "java.lang.StringBuilder";
	    String methodName = "charAt";
	    
		Method m = getMethod(className, methodName, new Class<?>[] { int.class });
		MetaMethod method = new MetaMethod(m, null);

		String bcSig = method.getSignatureForBytecode();
		
		Map<String, String> bytecodeMap = BytecodeLoader.fetchByteCodeForClass(new ArrayList<String>(), className);
		
		String methodBytecode = bytecodeMap.get(bcSig);
		
		assertNotNull(methodBytecode);		
	}
}
