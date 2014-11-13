/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class JITDataModel implements IReadOnlyJITDataModel
{
	private static final Logger logger = LoggerFactory.getLogger(JITDataModel.class);

	private PackageManager pm;
	private JITStats stats;

	// Not using CopyOnWriteArrayList as writes will vastly out number reads
	private List<JITEvent> jitEvents = new ArrayList<>();

	private Map<String, Journal> journalMap = new HashMap<>();

	// written during parse, make copy for graphing as needs sort
	private List<Tag> codeCacheTagList = new ArrayList<>();

	private String vmVersionRelease;

	public JITDataModel()
	{
		pm = new PackageManager();
		stats = new JITStats();
	}

	public void setVmVersionRelease(String release)
	{
		this.vmVersionRelease = release;
	}

	public String getVmVersionRelease()
	{
		return vmVersionRelease;
	}

	public void reset()
	{
		logger.info("JITDataModel.reset()");
		
		pm.clear();

		stats.reset();

		jitEvents.clear();

		journalMap.clear();

		codeCacheTagList.clear();
	}

	@Override
	public PackageManager getPackageManager()
	{
		return pm;
	}

	@Override
	public JITStats getJITStats()
	{
		return stats;
	}

	// ugly but better than using COWAL with so many writes
	public void addEvent(JITEvent event)
	{
		synchronized (jitEvents)
		{
			jitEvents.add(event);
		}
	}

	@Override
	public synchronized List<JITEvent> getEventListCopy()
	{
		synchronized (jitEvents)
		{
			return new ArrayList<>(jitEvents);
		}
	}

	public void addNativeBytes(long count)
	{
		stats.addNativeBytes(count);
	}

	public void updateStats(IMetaMember meta)
	{
		String fullSignature = meta.toString();

		for (String modifier : MODIFIERS)
		{
			if (fullSignature.contains(modifier + S_SPACE))
			{
				// use Java7 MethodHandle on JITStats object to increment
				// correct counter
				// probably slower than a set of 'if' statements but more
				// elegant :)

				String incMethodName = "incCount" + modifier.substring(0, 1).toUpperCase() + modifier.substring(1);

				try
				{
					MethodType mt = MethodType.methodType(void.class);

					MethodHandle mh = MethodHandles.lookup().findVirtual(JITStats.class, incMethodName, mt);

					mh.invokeExact(stats);
				}
				catch (Throwable t)
				{
					logger.error("Exception: {}", t.getMessage(), t);
				}
			}
		}

		String compiler = meta.getCompiledAttribute(ATTR_COMPILER);

		if (compiler != null)
		{
			if (C1.equalsIgnoreCase(compiler))
			{
				stats.incCountC1();
			}
			else if (C2.equalsIgnoreCase(compiler))
			{
				stats.incCountC2();
			}
		}

		String compileKind = meta.getCompiledAttribute(ATTR_COMPILE_KIND);

		if (compileKind != null)
		{
			if (OSR.equalsIgnoreCase(compileKind))
			{
				stats.incCountOSR();
			}
			else if (C2N.equalsIgnoreCase(compileKind))
			{
				stats.incCountC2N();
			}
		}

		String queueStamp = meta.getQueuedAttribute(ATTR_STAMP);
		String compileStamp = meta.getCompiledAttribute(ATTR_STAMP);

		if (queueStamp != null && compileStamp != null)
		{
			// convert decimal seconds into millis
			long queueMillis = ParseUtil.parseStamp(queueStamp);
			long compileMillis = ParseUtil.parseStamp(compileStamp);

			long delayMillis = compileMillis - queueMillis;

			meta.addCompiledAttribute("compileMillis", Long.toString(delayMillis));

			stats.recordDelay(delayMillis);
		}
	}

	public IMetaMember findMetaMember(MemberSignatureParts msp)
	{
		MetaClass metaClass = pm.getMetaClass(msp.getFullyQualifiedClassName());

		IMetaMember result = null;

		if (metaClass != null)
		{
			List<IMetaMember> metaList = metaClass.getMetaMembers();

			for (IMetaMember meta : metaList)
			{								
				if (meta.matchesSignature(msp))
				{
					result = meta;
					break;
				}
			}
		}
		else
		{
			logger.warn("No metaClass found for MemberSignatureParts {}", msp.getMemberName());
		}

		return result;
	}


	public MetaClass buildAndGetMetaClass(Class<?> clazz)
	{
		MetaClass resultMetaClass = null;
		
		String fqClassName = clazz.getName();
		
		String packageName;
		String className;

		int lastDotIndex = fqClassName.lastIndexOf(C_DOT);

		if (lastDotIndex != -1)
		{
			packageName = fqClassName.substring(0, lastDotIndex);
			className = fqClassName.substring(lastDotIndex + 1);
		}
		else
		{
			packageName = S_EMPTY;
			className = fqClassName;
		}

		MetaPackage mp = pm.getMetaPackage(packageName);

		if (mp == null)
		{
			mp = pm.buildPackage(packageName);
		}

		resultMetaClass = new MetaClass(mp, className);

		pm.addMetaClass(resultMetaClass);

		mp.addClass(resultMetaClass);

		stats.incCountClass();

		if (clazz.isInterface())
		{
			resultMetaClass.setInterface(true);
		}

		// Class.getDeclaredMethods() or Class.getDeclaredConstructors()
		// can cause a NoClassDefFoundError / ClassNotFoundException
		// for a parameter or return type.
		try
		{
			for (Method m : clazz.getDeclaredMethods())
			{
				MetaMethod metaMethod = new MetaMethod(m, resultMetaClass);
				resultMetaClass.addMetaMethod(metaMethod);
				stats.incCountMethod();
			}

			for (Constructor<?> c : clazz.getDeclaredConstructors())
			{
				MetaConstructor metaConstructor = new MetaConstructor(c, resultMetaClass);
				resultMetaClass.addMetaConstructor(metaConstructor);
				stats.incCountConstructor();
			}

		}
		catch (NoClassDefFoundError ncdfe)
		{
			logger.warn("NoClassDefFoundError: '{}' while building class {}", ncdfe.getMessage(), fqClassName);
			throw ncdfe;
		}
		catch (Throwable t)
		{
			logger.error("Something unexpected happened building meta class {}", fqClassName, t);
		}
		
		return resultMetaClass;
	}

	public void addCodeCacheTag(Tag tag)
	{
		synchronized (codeCacheTagList)
		{			
			codeCacheTagList.add(tag);
		}
	}

	@Override
	public List<Tag> getCodeCacheTags()
	{
		synchronized (codeCacheTagList)
		{
			return new ArrayList<>(codeCacheTagList);
		}
	}
}