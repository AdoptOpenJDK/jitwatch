/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.demo;

import com.chrisnewland.jitwatch.loader.BytecodeLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarScan {

    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private JarScan() {
    }

    @SuppressWarnings("unchecked")
    public static void iterateJar(File jarFile, int maxMethodBytes, PrintWriter writer) throws IOException {
        List<String> classLocations = new ArrayList<>();

        classLocations.add(jarFile.getPath());

        try (ZipFile zip = new ZipFile(jarFile)) {
            Enumeration<ZipEntry> list = (Enumeration<ZipEntry>) zip.entries();
            while (list.hasMoreElements()) {
                ZipEntry entry = list.nextElement();
                String name = entry.getName();

                if (name.endsWith(".class")) {
                    String fqName = name.replace("/", ".").substring(0, name.length() - 6);
                    process(classLocations, fqName, maxMethodBytes, writer);
                }
            }
            writer.flush();
        }
    }

    private static void process(List<String> classLocations, String className, int maxMethodBytes, PrintWriter writer) {
        Map<String, String> methodBytecode = BytecodeLoader.fetchByteCodeForClass(classLocations, className);

        boolean shownClass = false;

        for (Map.Entry<String, String> entry : methodBytecode.entrySet()) {
            String methodName = entry.getKey();
            String bytecode = entry.getValue();
            String[] lines = bytecode.split("\n");
            String lastLine = lines[lines.length - 1];
            String[] lastLineParts = lastLine.split(" ");
            String bcOffset = lastLineParts[0].substring(0, lastLineParts[0].length() - 1);

            // assume final instruction is a return of some kind for 1 byte
            int bcSize = 1 + tryParse(bcOffset);
            if (bcSize >= maxMethodBytes && !methodName.equals("static {}")) {
                if (!shownClass) {
                    writer.println(className);
                    shownClass = true;
                }

                writer.print(bcSize);
                writer.print(" -> ");
                writer.println(methodName);
            }

        }

    }

    private static int tryParse(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public static void main(String[] args) throws IOException {
        int maxMethodBytes = Integer.getInteger("maxMethodSize", 325);
        PrintWriter writer = new PrintWriter(System.out);
        for (String jar : args) {
            File jarFile = new File(jar);
            writer.print(jarFile.getAbsolutePath());
            writer.println(':');
            iterateJar(jarFile, maxMethodBytes, writer);
            writer.println();
        }
    }
}
