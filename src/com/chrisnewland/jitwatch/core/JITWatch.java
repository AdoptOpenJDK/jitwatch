package com.chrisnewland.jitwatch.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chrisnewland.jitwatch.meta.IMetaMember;
import com.chrisnewland.jitwatch.meta.MetaClass;
import com.chrisnewland.jitwatch.meta.MetaConstructor;
import com.chrisnewland.jitwatch.meta.MetaMethod;
import com.chrisnewland.jitwatch.meta.MetaPackage;
import com.chrisnewland.jitwatch.meta.PackageManager;

/**
 * To generate the log file used by JITWatch run your program with JRE switches
 * <code>-XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation -XX:+PrintAssembly</code>
 * 
 * http://dropzone.nfshost.com/hsdis.htm
 * https://wikis.oracle.com/display/HotSpotInternals/LogCompilation+overview
 */
public class JITWatch
{
	enum EventType
	{
		QUEUE, NMETHOD, TASK
	}

	private static final String TAG_TASK_QUEUED = "<task_queued compile_id";
	private static final String TAG_NMETHOD = "<nmethod";
	private static final String TAG_TASK = "<task compile_id";
	private static final String TAG_TASK_DONE = "<task_done";

	private static final String NATIVE_CODE_METHOD_MARK = "# {method}";

	private static final String LOADED = "[Loaded ";

	private static final String METHOD_START = "method='";

	private PackageManager pm;

	private boolean watching = false;

	private boolean inNativeCode = false;

	private StringBuilder nativeCodeBuilder = new StringBuilder();

	private IMetaMember currentMember = null;

	private IJITListener logListener = null;

	private long currentLineNumber;

	private JITStats stats = new JITStats();

	// Not using CopyOnWriteArrayList as writes will vastly out number reads
	private List<JITEvent> jitEvents = new ArrayList<>();

	private JITWatchConfig config;

	public JITWatch(IJITListener logListener, boolean mountSourcesAndClasses)
	{
		this.logListener = logListener;
		pm = new PackageManager();
		config = new JITWatchConfig(mountSourcesAndClasses, logListener);

		if (mountSourcesAndClasses)
		{
			for (String filename : config.getClassLocations())
			{
				addURIToClasspath(new File(filename).toURI());
			}
		}
	}

	private void addURIToClasspath(URI uri)
	{
		logListener.handleLogEntry("Adding classpath: " + uri.toString());

		try
		{
			// Try-with-resources on System classloader causes problems due to
			// closing?
			URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			URL url = uri.toURL();
			Class<?> urlClass = URLClassLoader.class;
			Method method = urlClass.getDeclaredMethod("addURL", new Class<?>[] { URL.class });
			method.setAccessible(true);
			method.invoke(urlClassLoader, new Object[] { url });
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	private void logEvent(JITEvent event)
	{
		if (logListener != null)
		{
			logListener.handleJITEvent(event);
		}
	}

	private void logError(String entry)
	{
		if (logListener != null)
		{
			logListener.handleErrorEntry(entry);
		}
	}

	public void watch(File hotspotLog) throws IOException
	{
		pm.clear();

		stats.reset();

		jitEvents.clear();

		currentLineNumber = 0;

		BufferedReader input = new BufferedReader(new FileReader(hotspotLog));

		String currentLine = null;

		watching = true;

		while (watching)
		{
			if (currentLine != null)
			{
				handleLine(currentLine);
			}
			else
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					break;
				}
			}

			currentLine = input.readLine();
		}

		input.close();
	}

	public void stop()
	{
		watching = false;
	}

	public PackageManager getPackageManager()
	{
		return pm;
	}

	private void handleLine(String currentLine)
	{
		currentLine = currentLine.replace("&apos;", "'");
		currentLine = currentLine.replace("&lt;", "<");
		currentLine = currentLine.replace("&gt;", ">");

		try
		{
			if (currentLine.startsWith(TAG_TASK_QUEUED))
			{
				if (inNativeCode)
				{
					completeNativeCode();
				}
				handleMethod(currentLine, EventType.QUEUE);
			}
			else if (currentLine.startsWith(TAG_NMETHOD))
			{
				if (inNativeCode)
				{
					completeNativeCode();
				}
				handleMethod(currentLine, EventType.NMETHOD);
			}
			else if (currentLine.startsWith(TAG_TASK))
			{
				if (inNativeCode)
				{
					completeNativeCode();
				}
				handleMethod(currentLine, EventType.TASK);
			}
			else if (currentLine.startsWith(TAG_TASK_DONE))
			{
				if (inNativeCode)
				{
					completeNativeCode();
				}
				handleTaskDone(currentLine);
			}
			else if (currentLine.startsWith(LOADED))
			{
				if (inNativeCode)
				{
					completeNativeCode();
				}
				handleLoaded(currentLine);
			}
			else if (currentLine.contains(NATIVE_CODE_METHOD_MARK))
			{
				String sig = convertNativeCodeMethodName(currentLine);

				currentMember = findMemberWithSignature(sig);
				inNativeCode = true;

				appendNativeCode(currentLine);

			}
			else if (inNativeCode)
			{
				appendNativeCode(currentLine);
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}

		currentLineNumber++;

	}

	private void appendNativeCode(String line)
	{
		nativeCodeBuilder.append(line).append("\n");
	}

	private void completeNativeCode()
	{
		inNativeCode = false;

		if (currentMember != null)
		{
			currentMember.setNativeCode(nativeCodeBuilder.toString());
		}

		nativeCodeBuilder.delete(0, nativeCodeBuilder.length());
	}

	private void handleMethod(String currentLine, EventType eventType)
	{
		Map<String, String> attrs = StringUtil.getLineAttributes(currentLine);

		String fqMethodName = StringUtil.getSubstringBetween(currentLine, METHOD_START, "'");

		if (fqMethodName != null)
		{
			fqMethodName = fqMethodName.replace("/", ".");

			boolean packageOK = config.isAllowedPackage(fqMethodName);

			if (packageOK)
			{
				attrs.remove("method");
				handleMethod(fqMethodName, attrs, eventType);
			}
		}
	}

	private IMetaMember findMemberWithSignature(String sig)
	{
		// <class> <method> (<params>)<return>
		// java/lang/String charAt (I)C

		IMetaMember metaMember = null;

		Matcher matcher = Pattern.compile("^([0-9a-zA-Z\\.\\$_]+) ([0-9a-zA-Z<>_\\$]+) (\\(.*\\))(.*)").matcher(sig);

		if (matcher.find())
		{
			String className = matcher.group(1);
			String methodName = matcher.group(2);
			String paramTypes = matcher.group(3).replace("(", "").replace(")", "");
			String returnType = matcher.group(4);

			Class<?>[] paramClasses = null;
			Class<?>[] returnClasses = null;

			try
			{
				paramClasses = ParseUtil.getClassTypes(paramTypes);
			}
			catch (Exception e)
			{
				logError(e.getMessage());
			}

			try
			{
				returnClasses = ParseUtil.getClassTypes(returnType); // expect 1
			}
			catch (Exception e)
			{
				logError(e.getMessage());
			}

			if (paramClasses != null && returnClasses != null)
			{
				Class<?> returnClass;

				if (returnClasses.length == 0)
				{
					returnClass = Void.class;
				}
				else
				{
					returnClass = returnClasses[0];
				}

				String signature = ParseUtil.buildMethodSignature(className, methodName, paramClasses, returnClass);

				if (signature != null)
				{
					MetaClass metaClass = pm.getMetaClass(className);

					if (metaClass != null)
					{
						List<IMetaMember> metaList = metaClass.getMetaMembers();

						for (IMetaMember meta : metaList)
						{
							if (meta.matches(signature))
							{
								metaMember = meta;
								break;
							}
						}
					}
				}
			}
			else
			{
				logError("Could not parse line " + currentLineNumber + " : " + sig);
			}
		}

		return metaMember;
	}

	private void handleMethod(String methodSignature, Map<String, String> attrs, EventType type)
	{
		IMetaMember metaMember = findMemberWithSignature(methodSignature);

		String stampAttr = attrs.get("stamp");
		long stampTime = (long) (Double.parseDouble(stampAttr) * 1000);

		if (metaMember != null)
		{
			switch (type)
			{
			case QUEUE:
				metaMember.setQueuedAttributes(attrs);
				JITEvent queuedEvent = new JITEvent(stampTime, false, metaMember.toString());
				addEvent(queuedEvent);
				logEvent(queuedEvent);
				break;
			case NMETHOD:
				metaMember.setCompiledAttributes(attrs);
				metaMember.getMetaClass().incCompiledMethodCount();
				updateStats(metaMember);

				JITEvent compiledEvent = new JITEvent(stampTime, true, metaMember.toString());
				addEvent(compiledEvent);
				logEvent(compiledEvent);
				break;
			case TASK:
				metaMember.addCompiledAttributes(attrs);
				currentMember = metaMember;
				break;
			}
		}
	}

	private void handleTaskDone(String line)
	{
		Map<String, String> attrs = StringUtil.getLineAttributes(line);

		if (attrs.containsKey("nmsize"))
		{
			long nmsize = Long.parseLong(attrs.get("nmsize"));
			stats.addNativeBytes(nmsize);
		}

		if (currentMember != null)
		{
			currentMember.addCompiledAttributes(attrs);
		}
	}

	// ugly but better than using COWAL with so many writes
	private synchronized void addEvent(JITEvent event)
	{
		jitEvents.add(event);
	}

	public synchronized List<JITEvent> getEventListCopy()
	{
		List<JITEvent> copy = new ArrayList<>(jitEvents);

		return copy;
	}

	private void updateStats(IMetaMember meta)
	{
		String fullSignature = meta.toString();

		for (String modifier : IMetaMember.MODIFIERS)
		{
			if (fullSignature.contains(modifier + " "))
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
					t.printStackTrace();
				}
			}
		}

		String compiler = meta.getCompiledAttribute("compiler");

		if (compiler != null)
		{
			if ("C1".equalsIgnoreCase(compiler))
			{
				stats.incCountC1();
			}
			else if ("C2".equalsIgnoreCase(compiler))
			{
				stats.incCountC2();
			}
		}

		String compileKind = meta.getCompiledAttribute("compile_kind");

		if (compileKind != null)
		{
			if ("osr".equalsIgnoreCase(compileKind))
			{
				stats.incCountOSR();
			}
			else if ("c2n".equalsIgnoreCase(compileKind))
			{
				stats.incCountC2N();
			}
		}

		String queueStamp = meta.getQueuedAttribute("stamp");
		String compileStamp = meta.getCompiledAttribute("stamp");

		if (queueStamp != null && compileStamp != null)
		{
			BigDecimal bdQ = new BigDecimal(queueStamp);
			BigDecimal bdC = new BigDecimal(compileStamp);

			BigDecimal delay = bdC.subtract(bdQ);

			BigDecimal delayMillis = delay.multiply(new BigDecimal("1000"));

			long delayLong = delayMillis.longValue();

			meta.addCompiledAttribute("compileMillis", Long.toString(delayLong));

			stats.recordDelay(delayLong);
		}
	}

	private void handleLoaded(String currentLine)
	{
		String fqClassName = StringUtil.getSubstringBetween(currentLine, LOADED, " ");

		if (fqClassName != null)
		{
			String packageName;
			String className;

			int lastDotIndex = fqClassName.lastIndexOf('.');

			if (lastDotIndex != -1)
			{
				packageName = fqClassName.substring(0, lastDotIndex);
				className = fqClassName.substring(lastDotIndex + 1);
			}
			else
			{
				packageName = "";
				className = fqClassName;
			}

			boolean packageOK = config.isAllowedPackage(packageName);

			if (packageOK)
			{
				MetaPackage mp = pm.getMetaPackage(packageName);

				if (mp == null)
				{
					mp = pm.buildPackage(packageName);
				}

				MetaClass metaClass = new MetaClass(mp, className);

				pm.addMetaClass(metaClass);

				mp.addClass(metaClass);

				try
				{
					Class<?> clazz = ParseUtil.loadClassWithoutInitialising(fqClassName);

					stats.incCountClass();

					if (clazz.isInterface())
					{
						metaClass.setInterface(true);
					}

					for (Method m : clazz.getDeclaredMethods())
					{
						MetaMethod metaMethod = new MetaMethod(m, metaClass);
						metaClass.addMetaMethod(metaMethod);
						stats.incCountMethod();
					}

					for (Constructor<?> c : clazz.getDeclaredConstructors())
					{
						MetaConstructor metaConstructor = new MetaConstructor(c, metaClass);
						metaClass.addMetaConstructor(metaConstructor);
						stats.incCountConstructor();
					}
				}
				catch (ClassNotFoundException cnf)
				{
					logError("ClassNotFoundException: " + fqClassName);
					metaClass.setMissingDef(true);
				}
				catch (NoClassDefFoundError ncdf)
				{
					logError("NoClassDefFoundError: " + fqClassName);
				}
			}
		}
	}

	public String convertNativeCodeMethodName(String name)
	{
		name = name.replace("'", "");

		int methodMarkIndex = name.indexOf(NATIVE_CODE_METHOD_MARK);

		if (methodMarkIndex != -1)
		{
			name = name.substring(methodMarkIndex + NATIVE_CODE_METHOD_MARK.length());
			name = name.trim();
		}

		String inToken = " in ";

		int inPos = name.indexOf(inToken);

		if (inPos != -1)
		{
			name = name.substring(inPos + inToken.length()) + " " + name.substring(0, inPos);
		}

		name = name.replaceAll("/", ".");

		return name;
	}

	public JITWatchConfig getConfig()
	{
		return config;
	}

	public JITStats getJITStats()
	{
		return stats;
	}
}