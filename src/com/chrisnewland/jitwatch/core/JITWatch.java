package com.chrisnewland.jitwatch.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chrisnewland.jitwatch.meta.MetaClass;
import com.chrisnewland.jitwatch.meta.MetaMethod;
import com.chrisnewland.jitwatch.meta.MetaPackage;
import com.chrisnewland.jitwatch.meta.PackageManager;

// To generate the log file used by JITWatch run your program with JRE switches
// -XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation -XX:+PrintAssembly 

//http://dropzone.nfshost.com/hsdis.htm
//https://wikis.oracle.com/display/HotSpotInternals/LogCompilation+overview

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

	private static final String JITWATCH_PROPERTIES = "jitwatch.properties";
	public static final String CONF_PACKAGE_FILTER = "PackageFilter";
	public static final String CONF_SOURCES = "Sources";
	public static final String CONF_CLASSES = "Classes";

	private static final String LOADED = "[Loaded ";

	private static final String METHOD_START = "method='";

	private String[] allowedPackages;
	private String[] sourceLocations;
	private String[] classLocations;

	private PackageManager pm;

	private boolean watching = false;

	private boolean inNativeCode = false;
	private StringBuilder nativeCodeBuilder = new StringBuilder();
	private MetaMethod currentMethod = null;

	private IJITListener logListener = null;

	private JITStats stats = new JITStats();

	// Not going to use a CopyOnWriteArrayList
	// as writes will vastly outnumber reads
	private List<JITEvent> jitEvents = new ArrayList<>();

	private boolean mountSourcesAndClasses;

	public JITWatch(IJITListener logListener, boolean mountSourcesAndClasses)
	{
		this.logListener = logListener;
		this.mountSourcesAndClasses = mountSourcesAndClasses;
		pm = new PackageManager();
		setProperties(getProperties());
	}

	public File getConfigFile()
	{
		return new File(System.getProperty("user.dir"), JITWATCH_PROPERTIES);
	}

	public void setProperties(Properties props)
	{
		String confPackages = (String) props.get(JITWatch.CONF_PACKAGE_FILTER);

		String confClasses = null;
		String confSources = null;
		
		if (mountSourcesAndClasses)
		{
			confClasses = (String) props.get(JITWatch.CONF_CLASSES);
			confSources = (String) props.get(JITWatch.CONF_SOURCES);
		}

		if (confPackages != null && confPackages.trim().length() > 0)
		{
			allowedPackages = confPackages.split(",");
		}
		else
		{
			allowedPackages = new String[0];
		}

		if (confClasses != null && confClasses.trim().length() > 0)
		{
			classLocations = confClasses.split(",");

			for (String filename : classLocations)
			{
				addURIToClasspath(new File(filename).toURI());
			}
		}

		if (confClasses != null && confClasses.trim().length() > 0)
		{
			sourceLocations = confSources.split(",");

			for (String source : sourceLocations)
			{
				log("Adding source: " + source);
			}
		}

		try
		{
			props.store(new FileWriter(getConfigFile()), null);
		}
		catch (IOException ioe)
		{
			log(ioe.toString());
		}
	}

	public Properties getProperties()
	{
		Properties jwProps = new Properties();

		try
		{
			jwProps.load(new FileReader(getConfigFile()));
		}
		catch (FileNotFoundException fnf)
		{

		}
		catch (IOException ioe)
		{
			log(ioe.toString());
		}

		return jwProps;
	}

	public static String unpack(String property)
	{
		if (property == null)
		{
			return "";
		}
		else
		{
			return property.replace(",", "\n");
		}
	}

	public static String pack(String property)
	{
		return property.trim().replace("\n", ",");
	}

	private void addURIToClasspath(URI uri)
	{
		log("Adding classpath: " + uri.toString());

		try
		{
			URL url = uri.toURL();
			URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
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

	private void log(String entry)
	{
		if (logListener != null)
		{
			logListener.handleLogEntry(entry);
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

				currentMethod = convertNMethodSig(sig, currentLine);
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

	}

	private void appendNativeCode(String line)
	{
		nativeCodeBuilder.append(line).append("\n");
	}

	private void completeNativeCode()
	{
		inNativeCode = false;

		if (currentMethod != null)
		{
			currentMethod.setNativeCode(nativeCodeBuilder.toString());
		}

		nativeCodeBuilder.delete(0, nativeCodeBuilder.length());
	}

	private Map<String, String> getAttributes(String line)
	{
		String[] spaceSep = line.split(" ");

		Map<String, String> result = new HashMap<>();

		for (String part : spaceSep)
		{
			String[] kvParts = part.split("=");

			if (kvParts.length == 2)
			{
				String key = kvParts[0];
				String value = getSubstringBetween(kvParts[1], "'", "'");

				result.put(key, value);
			}
		}

		return result;
	}

	private void handleMethod(String currentLine, EventType eventType)
	{
		Map<String, String> attrs = getAttributes(currentLine);

		String fqMethodName = getSubstringBetween(currentLine, METHOD_START, "'");

		if (fqMethodName != null)
		{
			fqMethodName = fqMethodName.replace("/", ".");

			boolean packageOK = allowedPackages.length == 0 || isAllowedPackage(fqMethodName);

			if (packageOK)
			{
				// fqMethodName = fqMethodName.replace("&lt;", "<");
				// fqMethodName = fqMethodName.replace("&gt;", ">");

				attrs.remove("method");
				handleMethod(fqMethodName, attrs, eventType, currentLine);
			}
		}
	}

	private MetaMethod convertNMethodSig(String sig, String currentLine)
	{
		// java/lang/String charAt (I)C

		MetaMethod metaMethod = null;

		Matcher matcher = Pattern.compile("^([0-9a-zA-Z\\.\\$_]+) ([0-9a-zA-Z<>_\\$]+) (\\(.*\\))(.*)").matcher(sig);

		if (matcher.find())
		{
			String className = matcher.group(1);
			String methodName = matcher.group(2);
			String paramTypes = matcher.group(3).replace("(", "").replace(")", "");
			String returnType = matcher.group(4);

			Class<?>[] paramClasses = getClassTypes(paramTypes);
			Class<?>[] returnClasses = getClassTypes(returnType); // expect 1

			Class<?> returnClass;

			if (returnClasses.length == 0)
			{
				returnClass = Void.class;
			}
			else
			{
				returnClass = returnClasses[0];
			}

			String signature = createSig(className, methodName, paramClasses, returnClass);

			if (signature != null)
			{
				MetaClass metaClass = pm.getMetaClass(className);

				if (metaClass != null)
				{
					List<MetaMethod> metaList = metaClass.getMetaMethods();

					for (MetaMethod meta : metaList)
					{
						if (meta.matches(signature))
						{
							metaMethod = meta;
							break;
						}
					}
				}
			}
		}
		else
		{
			logError("Could not parse line: " + currentLine);
		}

		return metaMethod;

	}

	private void handleMethod(String methodSignature, Map<String, String> attrs, EventType type, String currentLine)
	{
		MetaMethod metaMethod = convertNMethodSig(methodSignature, currentLine);

		String stampAttr = attrs.get("stamp");
		long stampTime = (long) (Double.parseDouble(stampAttr) * 1000);

		if (metaMethod != null)
		{
			switch (type)
			{
			case QUEUE:
				metaMethod.setQueuedAttributes(attrs);
				JITEvent queuedEvent = new JITEvent(stampTime, false, metaMethod.toString());
				addEvent(queuedEvent);
				logEvent(queuedEvent);
				break;
			case NMETHOD:
				metaMethod.setCompiledAttributes(attrs);
				metaMethod.getMetaClass().incCompiledMethodCount();
				updateStats(metaMethod);

				JITEvent compiledEvent = new JITEvent(stampTime, true, metaMethod.toString());
				addEvent(compiledEvent);
				logEvent(compiledEvent);
				break;
			case TASK:
				metaMethod.addCompiledAttributes(attrs);
				currentMethod = metaMethod;
				break;
			}
		}
	}

	private void handleTaskDone(String line)
	{
		Map<String, String> attrs = getAttributes(line);

		if (attrs.containsKey("nmsize"))
		{
			long nmsize = Long.parseLong(attrs.get("nmsize"));
			stats.addNativeBytes(nmsize);
		}

		if (currentMethod != null)
		{
			currentMethod.addCompiledAttributes(attrs);
		}
		else
		{
			logError("handleTaskDone: currentMethod not set?" + line);
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

	private void updateStats(MetaMethod meta)
	{
		String fullSignature = meta.toString();

		for (String modifier : MetaMethod.MODIFIERS)
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

	public String createSig(String className, String methodName, Class<?>[] paramTypes, Class<?> returnType)
	{
		StringBuilder builder = new StringBuilder();

		String rName = returnType.getName();
		rName = fixName(rName);

		builder.append(rName).append(" ").append(className).append(".").append(methodName);

		builder.append("(");

		for (Class<?> c : paramTypes)
		{
			String cName = c.getName();
			cName = fixName(cName);

			builder.append(cName).append(",");
		}

		if (paramTypes.length > 0)
		{
			builder.deleteCharAt(builder.length() - 1);
		}

		builder.append(")");

		String toMatch = builder.toString();

		return toMatch;
	}

	private String fixName(String name)
	{
		StringBuilder builder = new StringBuilder();

		int arrayDepth = 0;
		int pos = 0;

		outerloop: while (pos < name.length())
		{
			char c = name.charAt(pos);

			switch (c)
			{
			case '[':
				arrayDepth++;
				break;
			case 'S':
				builder.append("short");
				break;
			case 'C':
				builder.append("char");
				break;
			case 'B':
				builder.append("byte");
				break;
			case 'J':
				builder.append("long");
				break;
			case 'D':
				builder.append("double");
				break;
			case 'Z':
				builder.append("boolean");
				break;
			case 'I':
				builder.append("int");
				break;
			case 'F':
				builder.append("float");
				break;
			case ';':
				break;
			default:
				if (name.charAt(pos) == 'L' && name.endsWith(";"))
				{
					builder.append(name.substring(pos + 1, name.length() - 1));
				}
				else
				{
					builder.append(name.substring(pos));
				}
				break outerloop;
			}

			pos++;
		}

		for (int i = 0; i < arrayDepth; i++)
		{
			builder.append("[]");
		}

		return builder.toString();
	}

	private Class<?>[] getClassTypes(String types)
	{
		List<Class<?>> classes = new ArrayList<Class<?>>();

		final int typeLen = types.length();

		if (typeLen > 0)
		{
			StringBuilder builder = new StringBuilder();

			try
			{
				int pos = 0;

				while (pos < types.length())
				{
					char c = types.charAt(pos);

					switch (c)
					{
					case '[':
						// Could be
						// [Ljava.lang.String; Object array
						// [I primitive array
						// [..[I (multidimensional primitive array
						// [..[Ljava.lang.String multidimensional Object array
						builder.delete(0, builder.length());
						builder.append(c);
						pos++;
						c = types.charAt(pos);

						while (c == '[')
						{
							builder.append(c);
							pos++;
							c = types.charAt(pos);
						}

						if (c == 'L')
						{
							// array of ref type
							while (pos < typeLen)
							{
								c = types.charAt(pos++);
								builder.append(c);

								if (c == ';')
								{
									break;
								}
							}
						}
						else
						{
							// array of primitive
							builder.append(c);
							pos++;
						}

						Class<?> arrayClass = loadClassWithoutInitialising(builder.toString());
						classes.add(arrayClass);
						builder.delete(0, builder.length());
						break;
					case 'L':
						// ref type
						while (pos < typeLen)
						{
							pos++;
							c = types.charAt(pos);

							if (c == ';')
							{
								pos++;
								break;
							}

							builder.append(c);
						}
						Class<?> refClass = loadClassWithoutInitialising(builder.toString());
						classes.add(refClass);
						builder.delete(0, builder.length());
						break;
					default:
						// primitive
						Class<?> primitiveClass = getPrimitiveClass(c);
						classes.add(primitiveClass);
						pos++;

					} // end switch

				} // end while

			}
			catch (ClassNotFoundException cnf)
			{
				logError("ClassNotFoundException: " + builder.toString());
			}
			catch (NoClassDefFoundError ncdf)
			{
				logError("NoClassDefFoundError: " + builder.toString());
			}
			catch (Exception ex)
			{
				logError("Exception: " + ex.getMessage());
			}
			catch (Error err)
			{
				logError("Error: " + err.getMessage());
			}

		} // end if empty

		return classes.toArray(new Class<?>[classes.size()]);
	}

	private Class<?> getPrimitiveClass(char c)
	{
		switch (c)
		{
		case 'S':
			return Short.TYPE;
		case 'C':
			return Character.TYPE;
		case 'B':
			return Byte.TYPE;
		case 'V':
			return Void.TYPE;
		case 'J':
			return Long.TYPE;
		case 'D':
			return Double.TYPE;
		case 'Z':
			return Boolean.TYPE;
		case 'I':
			return Integer.TYPE;
		case 'F':
			return Float.TYPE;
		}

		throw new RuntimeException("Unknown class for " + c);
	}

	private boolean isAllowedPackage(String packageName)
	{
		for (String allowedPackage : allowedPackages)
		{
			if (allowedPackage.equals(packageName) || packageName.startsWith(allowedPackage + "."))
			{
				return true;
			}
		}

		return false;
	}

	private Class<?> loadClassWithoutInitialising(String fqClassName) throws ClassNotFoundException
	{
		try
		{
			return Class.forName(fqClassName, false, this.getClass().getClassLoader());
		}
		catch (Throwable t)
		{
			throw t;
		}
	}

	private void handleLoaded(String currentLine)
	{
		String fqClassName = getSubstringBetween(currentLine, LOADED, " ");

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

			boolean packageOK = allowedPackages.length == 0 || isAllowedPackage(packageName);

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
					Class<?> clazz = loadClassWithoutInitialising(fqClassName);

					if (clazz.isInterface())
					{
						metaClass.setInterface(true);
					}

					Method[] declaredMethods = clazz.getDeclaredMethods();

					for (Method m : declaredMethods)
					{
						MetaMethod metaMethod = new MetaMethod(m, metaClass);
						metaClass.addMetaMethod(metaMethod);
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

	private String getSubstringBetween(String input, String start, String end)
	{
		int startPos = input.indexOf(start);

		String result = null;

		if (startPos != -1)
		{
			int endPos = input.indexOf(end, startPos + start.length());

			if (endPos != -1)
			{
				result = input.substring(startPos + start.length(), endPos);
			}

		}

		return result;
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

	public String[] getClassLocations()
	{
		return classLocations;
	}

	public String[] getSourceLocations()
	{
		return sourceLocations;
	}

	public JITStats getJITStats()
	{
		return stats;
	}
}