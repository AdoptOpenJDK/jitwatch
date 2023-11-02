package org.adoptopenjdk.jitwatch.test;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.process.compiler.ICompiler;
import org.adoptopenjdk.jitwatch.process.runtime.IRuntime;
import org.junit.Before;
import org.junit.Test;
import org.adoptopenjdk.jitwatch.jvmlang.LanguageManager;
import org.mockito.Mockito;
import java.io.File;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import static org.junit.Assert.*;

public class TestLanguageManager {

    /**
     * test: to get language from file
     */
    @Test
    public void testIsCompilable() {
        // with a valid language and file extension
        assertTrue(LanguageManager.isCompilable(VM_LANGUAGE_JAVA, new File("file.java")));

        // with a different language
        assertFalse(LanguageManager.isCompilable(VM_LANGUAGE_JAVA,  new File("file.scala")));

        // with a file that has no extension
        assertFalse(LanguageManager.isCompilable(VM_LANGUAGE_JAVA, new File("noextensione")));
    }

    /**
     * test: to get file extension
     */
    @Test
    public void testGetFileExtension() {
        // with a file that has an extension
        String extension = LanguageManager.getFileExtension(new File("sample.txt"));
        assertEquals("txt", extension);

        // with a file that has no extension
        String extension1 = LanguageManager.getFileExtension(new File("filewithoutextension"));
        assertNull(extension1);

        // with a file that has two parts
        String extension3 = LanguageManager.getFileExtension(new File("sample.zip.tar"));
        assertEquals("tar", extension3);

        // with a file path
        String extension4 = LanguageManager.getFileExtension(new File("/path/to/dir/sample.jpg"));
        assertEquals("jpg", extension4);

        // with a null file
        String extension5 = LanguageManager.getFileExtension(null);
        assertNull(extension5);
    }

    /**
     * test: to get language from file
     */
    @Test
    public void testGetLanguageFromFile() {
        // with extension
        File javaFile = new File("sample.java");
        assertEquals(VM_LANGUAGE_JAVA, LanguageManager.getLanguageFromFile(javaFile));

        // with a different extension
        File scalaFile = new File("sample.scala");
        assertEquals(VM_LANGUAGE_SCALA, LanguageManager.getLanguageFromFile(scalaFile));

        // with incorrect extension
        File unknownFile = new File("sample.abc");
        assertNull(LanguageManager.getLanguageFromFile(unknownFile));

        // with a null file
        assertNull(LanguageManager.getLanguageFromFile(null));
    }

    /**
     * test: to get only known filename extensions
     */

    @Test
    public void testGetKnownFilenameExtensions() {
        // with known filename extensions
        assertTrue(LanguageManager.getKnownFilenameExtensions().contains("java"));
        assertTrue(LanguageManager.getKnownFilenameExtensions().contains("scala"));
        assertTrue(LanguageManager.getKnownFilenameExtensions().contains("rb"));
        assertTrue(LanguageManager.getKnownFilenameExtensions().contains("js"));
        assertTrue(LanguageManager.getKnownFilenameExtensions().contains("kt"));
        assertTrue(LanguageManager.getKnownFilenameExtensions().contains("groovy"));
        assertTrue(LanguageManager.getKnownFilenameExtensions().contains("gvy"));
        assertTrue(LanguageManager.getKnownFilenameExtensions().contains("gy"));
        assertTrue(LanguageManager.getKnownFilenameExtensions().contains("clj"));
        // with unknown filename extensions
        assertFalse(LanguageManager.getKnownFilenameExtensions().contains("xyz"));
        assertFalse(LanguageManager.getKnownFilenameExtensions().contains("abc"));
        // with no empty string
        assertFalse(LanguageManager.getKnownFilenameExtensions().contains(""));
        // with null input
        assertFalse(LanguageManager.getKnownFilenameExtensions().contains(null));
    }

    /**
     * test: to check if language is enabled
     */
    @Test
    public void testIsLanguageEnabled() {
        // if a language is enabled
        assertTrue(LanguageManager.isLanguageEnabled(VM_LANGUAGE_JAVA));
        assertFalse(LanguageManager.isLanguageEnabled(VM_LANGUAGE_CLOJURE));
        assertTrue(LanguageManager.isLanguageEnabled(VM_LANGUAGE_SCALA));

        // for unknown language
        assertFalse(LanguageManager.isLanguageEnabled("language"));
        assertFalse(LanguageManager.isLanguageEnabled("python"));

        // for empty string
        assertFalse(LanguageManager.isLanguageEnabled(""));
    }

    /**
     * test: for null string
     */
    @Test
    public void nullHandlerForLanguage() {
        assertFalse(LanguageManager.isLanguageEnabled(null));
    }
}