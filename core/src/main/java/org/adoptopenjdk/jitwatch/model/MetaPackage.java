/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class MetaPackage implements Comparable<MetaPackage>
{
    private String packageName;

    private List<MetaPackage> childPackages = new CopyOnWriteArrayList<>();
    private List<MetaClass> packageClasses = new CopyOnWriteArrayList<>();

    // support navigating back up the tree
    private MetaPackage parentPackage = null;

    private boolean hasCompiledClasses = false;

    public MetaPackage(String packageName)
    {
        this.packageName = packageName;
    }

    public List<MetaPackage> getChildPackages()
    {
        MetaPackage[] asArray = childPackages.toArray(new MetaPackage[childPackages.size()]);
        Arrays.sort(asArray);
        return new ArrayList<>(Arrays.asList(asArray));
    }

    public void addChildPackage(MetaPackage child)
    {
        child.setParentPackage(this);
        childPackages.add(child);
    }

    public MetaPackage getChildPackage(String name)
    {
        for (MetaPackage mp : childPackages)
        {
            if (mp.getName().equals(name))
            {
                return mp;
            }
        }

        return null;
    }

    public List<MetaClass> getPackageClasses()
    {
        MetaClass[] asArray = packageClasses.toArray(new MetaClass[packageClasses.size()]);
        Arrays.sort(asArray);
        return new ArrayList<>(Arrays.asList(asArray));
    }

    public List<String> getPackageComponents()
    {
        List<String> components = new ArrayList<>();

        if (packageName.indexOf('.') == -1)
        {
            components.add(packageName);
        }
        else
        {
            components.addAll(Arrays.asList(packageName.split("\\.")));
        }

        return components;
    }

    public void addClass(MetaClass metaClass)
    {
        packageClasses.add(metaClass);
    }

    public String getName()
    {
        return packageName;
    }

    @Override
    public String toString()
    {
    	String str;
    	
    	if (S_EMPTY.equals(packageName))
    	{
    		str = DEFAULT_PACKAGE_NAME;
    	}
    	else
    	{
    		str = getName();
    	}
    	
        return str;
    }

    @Override
    public int compareTo(MetaPackage other)
    {
        return this.getName().compareTo(other.getName());
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else
        {
            return toString().equals(obj.toString());
        }
    }

    public MetaPackage getParentPackage()
    {
        return parentPackage;
    }

    public void setParentPackage(MetaPackage parentPackage)
    {
        this.parentPackage = parentPackage;
    }

    public boolean hasCompiledClasses()
    {
        return hasCompiledClasses;
    }

    public void setHasCompiledClasses()
    {
        if (!hasCompiledClasses)
        {
            hasCompiledClasses = true;

            // percolate upwards
            if (parentPackage != null && !parentPackage.hasCompiledClasses)
            {
                parentPackage.setHasCompiledClasses();
            }
        }
    }
}