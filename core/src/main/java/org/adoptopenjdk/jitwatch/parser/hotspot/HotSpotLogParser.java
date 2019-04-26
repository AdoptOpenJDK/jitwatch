/*
 * Copyright (c) 2013-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.parser.hotspot;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_THREAD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_AT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_SQUARE_BRACKET;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_ASSEMBLY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.LOADED;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.SKIP_BODY_TAGS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.SKIP_HEADER_TAGS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_AT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_FILE_COLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CLOSE_CDATA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CODE_CACHE_FULL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_COMMAND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_HOTSPOT_LOG_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OPEN_CDATA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_OPEN_CLOSE_CDATA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PRINT_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_RELEASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_START_COMPILE_THREAD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_SWEEPER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_QUEUED;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_VM_ARGUMENTS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_VM_VERSION;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_WRITER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_XML;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_HOTSPOT_LOG;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_TIME_MS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.model.CodeCacheEvent.CodeCacheEventType;
import org.adoptopenjdk.jitwatch.model.NumberedLine;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyProcessor;
import org.adoptopenjdk.jitwatch.parser.AbstractLogParser;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.adoptopenjdk.jitwatch.util.VmVersionDetector;

public class HotSpotLogParser extends AbstractLogParser
{
    public HotSpotLogParser(IJITListener jitListener)
    {
        super(jitListener);
    }

    private void checkIfErrorDialogNeeded()
    {
        if (hasParseError)
        {
            errorListener.handleError(errorDialogTitle, errorDialogBody);
        }
    }

    private void parseHeaderLines()
    {
        if (DEBUG_LOGGING)
        {
            logger.debug("parseHeaderLines()");
        }

        for (NumberedLine numberedLine : splitLog.getHeaderLines())
        {
            String lineContent = numberedLine.getLine();

            if (!skipLine(lineContent, SKIP_HEADER_TAGS))
            {
                Tag tag = tagProcessor.processLine(lineContent);

                processLineNumber = numberedLine.getLineNumber();

                if (tag != null)
                {
                    handleTag(tag);
                }
            }
            else if (lineContent.startsWith(TAG_HOTSPOT_LOG))
            {
                long baseTimestamp = getBaseTimestamp(lineContent);

                model.setBaseTimestamp(baseTimestamp);
            }
        }
    }

    private long getBaseTimestamp(String line)
    {
        String attributePart = line.substring(TAG_HOTSPOT_LOG.length());

        Map<String, String> attrs = StringUtil.attributeStringToMap(attributePart);

        String baseTimestampAttr = attrs.get(ATTR_TIME_MS);

        return ParseUtil.parseStamp(baseTimestampAttr) / 1000;
    }

    @Override
    protected void parseLogFile()
    {
        parseHeaderLines();

        buildParsedClasspath();

        buildClassModel();

        parseLogCompilationLines();

        parseAssemblyLines();

        checkIfErrorDialogNeeded();
    }

    private void parseLogCompilationLines()
    {
        if (DEBUG_LOGGING)
        {
            logger.debug("parseLogCompilationLines()");
        }

        for (NumberedLine numberedLine : splitLog.getCompilationLines())
        {
            if (!skipLine(numberedLine.getLine(), SKIP_BODY_TAGS))
            {
                Tag tag = tagProcessor.processLine(numberedLine.getLine());

                processLineNumber = numberedLine.getLineNumber();

                if (tag != null)
                {
                    handleTag(tag);
                }
            }
        }
    }

    private void parseAssemblyLines()
    {
        if (DEBUG_LOGGING_ASSEMBLY)
        {
            logger.debug("parseAssemblyLines()");
        }

        AssemblyProcessor asmProcessor = new AssemblyProcessor();

        for (NumberedLine numberedLine : splitLog.getAssemblyLines())
        {
            processLineNumber = numberedLine.getLineNumber();

            asmProcessor.handleLine(numberedLine.getLine());
        }

        asmProcessor.complete();

        asmProcessor.attachAssemblyToMembers(model.getPackageManager());

        asmProcessor.clear();
    }

    @Override
    protected void splitLogFile(File hotspotLog)
    {
        reading = true;

        try (BufferedReader reader = new BufferedReader(new FileReader(hotspotLog), 65536))
        {
            String currentLine = reader.readLine();

            while (reading && currentLine != null)
            {
                try
                {
                    String trimmedLine = currentLine.trim();

                    if (trimmedLine.length() > 0)
                    {
                        char firstChar = trimmedLine.charAt(0);

                        if (firstChar == C_OPEN_ANGLE || firstChar == C_OPEN_SQUARE_BRACKET || firstChar == C_AT)
                        {
                            currentLine = trimmedLine;
                        }

                        handleLogLine(currentLine);
                    }
                }
                catch (Exception ex)
                {
                    logger.error("Exception handling: '{}'", currentLine, ex);
                }

                currentLine = reader.readLine();
            }
        }
        catch (IOException ioe)
        {
            logger.error("Exception while splitting log file", ioe);
        }
    }

    private boolean skipLine(final String line, final Set<String> skipSet)
    {
        boolean isSkip = false;

        for (String skip : skipSet)
        {
            if (line.startsWith(skip))
            {
                isSkip = true;
                break;
            }
        }

        return isSkip;
    }

    private void handleLogLine(final String inCurrentLine)
    {
        String currentLine = inCurrentLine;

        NumberedLine numberedLine = new NumberedLine(parseLineNumber++, currentLine);

        if (TAG_TTY.equals(currentLine))
        {
            inHeader = false;
            return;
        }
        else if (currentLine.startsWith(TAG_XML))
        {
            inHeader = true;
        }

        if (inHeader)
        {
            // HotSpot log header XML can have text nodes so consume all lines
            splitLog.addHeaderLine(numberedLine);
        }
        else
        {
            if (currentLine.startsWith(TAG_OPEN_CDATA) || currentLine.startsWith(TAG_CLOSE_CDATA)
                    || currentLine.startsWith(TAG_OPEN_CLOSE_CDATA))
            {
                // ignore, TagProcessor will recognise from <fragment> tag
            }
            else if (currentLine.startsWith(S_OPEN_ANGLE))
            {
                // After the header, XML nodes do not have text nodes
                splitLog.addCompilationLine(numberedLine);
            }
            else if (currentLine.startsWith(LOADED))
            {
                splitLog.addClassLoaderLine(numberedLine);
            }
            else if (currentLine.startsWith(S_AT))
            {
                // possible PrintCompilation was enabled as well as
                // LogCompilation?
                // jmh does this with perf annotations
                // Ignore this line
            }
            else if (currentLine.indexOf(S_OPEN_ANGLE + TAG_NMETHOD) != -1)
            {
                // need to cope with nmethod appearing on same line as last hlt
                // 0x0000 hlt <nmethod compile_id= ....

                int indexNMethod = currentLine.indexOf(S_OPEN_ANGLE + TAG_NMETHOD);

                if (DEBUG_LOGGING)
                {
                    logger.debug("detected nmethod tag mangled with assembly");
                }

                String assembly = currentLine.substring(0, indexNMethod);

                String remainder = currentLine.substring(indexNMethod);

                numberedLine.setLine(assembly);

                splitLog.addAssemblyLine(numberedLine);

                handleLogLine(remainder);

            }
            else if (currentLine.indexOf(S_OPEN_ANGLE + S_SLASH + TAG_PRINT_NMETHOD) != -1)
            {
                // need to cope with </print_nmethod> appearing on same last as
                // last assembly statement
                // ImmutableOopMap{rsi=Oop }pc offsets: 182 192 197 206 215
                // </print_nmethod>

                int indexClosePrintNmethod = currentLine.indexOf(S_OPEN_ANGLE + S_SLASH + TAG_PRINT_NMETHOD);

                if (DEBUG_LOGGING)
                {
                    logger.debug("detected </print_nmethod> tag mangled with assembly");
                }

                String assembly = currentLine.substring(0, indexClosePrintNmethod);

                String remainder = currentLine.substring(indexClosePrintNmethod);

                numberedLine.setLine(assembly);

                splitLog.addAssemblyLine(numberedLine);

                handleLogLine(remainder);

            }
            else
            {
                splitLog.addAssemblyLine(numberedLine);
            }
        }
    }

    @Override
    protected void handleTag(Tag tag)
    {
        String tagName = tag.getName();

        switch (tagName)
        {

        case TAG_WRITER:
            handleWriterThread(tag);
            break;

        case TAG_VM_VERSION:
            handleVmVersion(tag);
            break;

        case TAG_TASK_QUEUED:
            handleTagQueued(tag);
            break;

        case TAG_NMETHOD:
            handleTagNMethod(tag);
            break;

        case TAG_TASK:
            handleTagTask((Task) tag);
            break;

        case TAG_SWEEPER:
            storeCodeCacheEvent(CodeCacheEventType.SWEEPER, tag);
            break;

        case TAG_CODE_CACHE_FULL:
            storeCodeCacheEvent(CodeCacheEventType.CACHE_FULL, tag);
            break;

        case TAG_HOTSPOT_LOG_DONE:
            model.setEndOfLog(tag);
            break;

        case TAG_START_COMPILE_THREAD:
            handleStartCompileThread(tag);
            break;

        case TAG_VM_ARGUMENTS:
            handleTagVmArguments(tag);
            break;

        default:
            break;
        }
    }

    private void handleVmVersion(Tag tag)
    {
        model.setJDKMajorVersion(VmVersionDetector.getMajorVersionFromHotSpotTag(tag));
    }

    private void handleTagVmArguments(Tag tag)
    {
        List<Tag> tagCommandChildren = tag.getNamedChildren(TAG_COMMAND);

        if (tagCommandChildren.size() > 0)
        {
            vmCommand = tagCommandChildren.get(0).getTextContent();

            if (DEBUG_LOGGING)
            {
                logger.debug("VM Command: {}", vmCommand);
            }
        }
    }

    private void handleWriterThread(Tag tag)
    {
        String threadId = tag.getAttributes().get(ATTR_THREAD);

        if (threadId != null)
        {
            // currentCompilerThread = model.getCompilerThread(threadId);
        }
    }

    private void handleStartCompileThread(Tag tag)
    {
        // <start_compile_thread name='C2 CompilerThread1' thread='17667'
        // process='82237' stamp='0.079'/>

        String threadId = tag.getAttributes().get(ATTR_THREAD);
        String threadName = tag.getAttributes().get(ATTR_NAME);

        if (threadId != null)
        {
            currentCompilerThread = model.createCompilerThread(threadId, threadName);
        }
    }

    private void buildParsedClasspath()
    {
        if (DEBUG_LOGGING)
        {
            logger.debug("buildParsedClasspath()");
        }

        for (NumberedLine numberedLine : splitLog.getClassLoaderLines())
        {
            buildParsedClasspath(numberedLine.getLine());
        }
    }

    private void buildClassModel()
    {
        if (DEBUG_LOGGING)
        {
            logger.debug("buildClassModel()");
        }

        for (NumberedLine numberedLine : splitLog.getClassLoaderLines())
        {
            buildClassModel(numberedLine.getLine());
        }
    }

    private void buildParsedClasspath(String inCurrentLine)
    {
        final String FROM_SPACE = "from ";

        String originalLocation = null;

        int fromSpacePos = inCurrentLine.indexOf(FROM_SPACE);

        if (fromSpacePos != -1)
        {
            originalLocation = inCurrentLine.substring(fromSpacePos + FROM_SPACE.length(), inCurrentLine.length() - 1);
        }

        if (originalLocation != null && originalLocation.startsWith(S_FILE_COLON))
        {
            originalLocation = originalLocation.substring(S_FILE_COLON.length());

            try
            {
                originalLocation = URLDecoder.decode(originalLocation, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                // ignore
            }

            getParsedClasspath().addClassLocation(originalLocation);
        }
    }

    private void buildClassModel(String inCurrentLine)
    {
        String fqClassName = StringUtil.getSubstringBetween(inCurrentLine, LOADED, S_SPACE);

        if (fqClassName != null)
        {
            addToClassModel(fqClassName);
        }
    }
}
