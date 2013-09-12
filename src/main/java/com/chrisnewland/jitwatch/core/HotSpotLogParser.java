package com.chrisnewland.jitwatch.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.JITDataModel;
import com.chrisnewland.jitwatch.util.ClassUtil;
import com.chrisnewland.jitwatch.util.ParseUtil;
import com.chrisnewland.jitwatch.util.StringUtil;

/**
 * To generate the log file used by JITWatch run your program with JRE switches
 * <code>-XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation -XX:+PrintAssembly</code>
 * 
 * http://dropzone.nfshost.com/hsdis.htm
 * https://wikis.oracle.com/display/HotSpotInternals/LogCompilation+overview
 */
public class HotSpotLogParser
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

    private JITDataModel model;

    private boolean watching = false;

    private boolean inNativeCode = false;

    private StringBuilder nativeCodeBuilder = new StringBuilder();

    private IMetaMember currentMember = null;

    private IJITListener logListener = null;

    private long currentLineNumber;

    private JITWatchConfig config;

    public HotSpotLogParser(JITDataModel model, JITWatchConfig config, IJITListener logListener)
    {
        this.model = model;

        this.logListener = logListener;

        this.config = config;
    }

    private void mountAdditionalClasses()
    {
        for (String filename : config.getClassLocations())
        {
            URI uri = new File(filename).toURI();

            logListener.handleLogEntry("Adding classpath: " + uri.toString());

            ClassUtil.addURIToClasspath(uri);
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
        mountAdditionalClasses();

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

    private IMetaMember findMemberWithSignature(String logSignature)
    {
        IMetaMember metaMember = null;

        String[] parsedResult = null;

        try
        {
            parsedResult = ParseUtil.parseLogSignature(logSignature);
        }
        catch (Exception e)
        {
            logError(e.getMessage());
        }

        if (parsedResult != null)
        {
            String className = parsedResult[0];
            String parsedSignature = parsedResult[1];

            if (parsedSignature != null)
            {
                metaMember = model.findMetaMember(className, parsedSignature);
            }
        }
        else
        {
            logError("Could not parse line " + currentLineNumber + " : " + logSignature);
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
                model.addEvent(queuedEvent);
                logEvent(queuedEvent);
                break;
            case NMETHOD:
                metaMember.setCompiledAttributes(attrs);
                metaMember.getMetaClass().incCompiledMethodCount();
                model.updateStats(metaMember);

                JITEvent compiledEvent = new JITEvent(stampTime, true, metaMember.toString());
                model.addEvent(compiledEvent);
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
            model.addNativeBytes(nmsize);
        }

        if (currentMember != null)
        {
            currentMember.addCompiledAttributes(attrs);
        }
    }

    /*
     * JITWatch needs classloader information so it can show classes which have
     * no JIT-compiled methods in the class tree
     */
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

            boolean allowedPackage = config.isAllowedPackage(packageName);

            if (allowedPackage)
            {
                Class<?> clazz = null;

                try
                {
                    clazz = ClassUtil.loadClassWithoutInitialising(fqClassName);
                }
                catch (ClassNotFoundException cnf)
                {
                    logError("ClassNotFoundException: " + fqClassName);
                }
                catch (NoClassDefFoundError ncdf)
                {
                    logError("NoClassDefFoundError: " + fqClassName);
                }

                model.buildMetaClass(packageName, className, clazz);
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
}