/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import org.adoptopenjdk.jitwatch.loader.DisposableURLClassLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public final class ClassUtil
{
  private static DisposableURLClassLoader disposableClassLoader = new DisposableURLClassLoader(new URL[0]);
  private static Set<URL> urls;

  private ClassUtil()
  {
  }

  public static void initialise(URL[] inUrls)
  {
    urls = new HashSet<URL>();
    for (URL url: inUrls) {
      urls.add(url);
    }
    disposableClassLoader = new DisposableURLClassLoader(inUrls);
  }

  public static Class<?> loadClassWithoutInitialising(String fqClassName, String location)
      throws ClassNotFoundException
  {
    if (location != null) {
      try {
        URL url = new URL("file://" + location);
        if (!urls.contains(url)) {
          urls.add(url);
          URL[] inputs = urls.toArray(new URL[urls.size()]);
          disposableClassLoader = new DisposableURLClassLoader(inputs);
        }
      } catch (MalformedURLException ex) {
        // skip
      }
    }

    return loadClassWithoutInitialising(fqClassName);
  }

  public static Class<?> loadClassWithoutInitialising(String fqClassName) throws ClassNotFoundException
  {
    return Class.forName(fqClassName, false, disposableClassLoader);
  }
}
