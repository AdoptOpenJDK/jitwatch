/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.model;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.core.JITEvent;
import com.chrisnewland.jitwatch.core.JITStats;

public class JITDataModel
{
    private PackageManager pm;
    private JITStats stats;

    // Not using CopyOnWriteArrayList as writes will vastly out number reads
    private List<JITEvent> jitEvents = new ArrayList<>();

    private Map<String, Journal> journalMap = new HashMap<>();

    public JITDataModel()
    {
        pm = new PackageManager();
        stats = new JITStats();
    }

    public void reset()
    {
        pm.clear();

        stats.reset();

        jitEvents.clear();

        journalMap.clear();
    }

    public PackageManager getPackageManager()
    {
        return pm;
    }

    public JITStats getJITStats()
    {
        return stats;
    }

    public void addJournalEntry(String id, String entry)
    {
        Journal journal = journalMap.get(id);

        if (journal == null)
        {
            journal = new Journal();
            journalMap.put(id, journal);
        }
        
        journal.addEntry(entry);
    }

    // can we guarantee that IMetaMember will be created before
    // journal entries are ready? Assume not so store in model
    // instead of member
    public Journal getJournal(String id)
    {
        Journal journal = journalMap.get(id);

        if (journal == null)
        {
            journal = new Journal();
            journalMap.put(id, journal);
        }
        
        return journal;
    }

    // ugly but better than using COWAL with so many writes
    public synchronized void addEvent(JITEvent event)
    {
        jitEvents.add(event);
    }

    public synchronized List<JITEvent> getEventListCopy()
    {
        List<JITEvent> copy = new ArrayList<>(jitEvents);

        return copy;
    }

    public void addNativeBytes(long count)
    {
        stats.addNativeBytes(count);
    }

    public void updateStats(IMetaMember meta)
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

    public IMetaMember findMetaMember(String className, String signature)
    {
        MetaClass metaClass = pm.getMetaClass(className);

        IMetaMember result = null;

        if (metaClass != null)
        {
            List<IMetaMember> metaList = metaClass.getMetaMembers();

            for (IMetaMember meta : metaList)
            {
                if (meta.matches(signature))
                {
                    result = meta;
                    break;
                }
            }
        }

        return result;
    }

    public void buildMetaClass(String packageName, String className, Class<?> clazz)
    {
        MetaPackage mp = pm.getMetaPackage(packageName);

        if (mp == null)
        {
            mp = pm.buildPackage(packageName);
        }

        MetaClass metaClass = new MetaClass(mp, className);

        pm.addMetaClass(metaClass);

        mp.addClass(metaClass);

        stats.incCountClass();

        if (clazz == null)
        {
            metaClass.setMissingDef(true);
        }
        else
        {
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
    }
}
