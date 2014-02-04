/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.histo;

import java.util.List;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.MetaPackage;

public class HistoTreeWalker
{
	// Good case for J8 Streams
	public static Histo buildHistogram(IHistoWalker walker)
	{
		Histo histo = new Histo(walker.getResolution());
		
		walker.reset();

		List<MetaPackage> roots = walker.getJITDataModel().getPackageManager().getRootPackages();

		for (MetaPackage mp : roots)
		{
			walkTree(mp, histo, walker);
		}

		return histo;
	}

	private static void walkTree(MetaPackage mp, Histo histo, IHistoWalker walker)
	{
		List<MetaPackage> childPackages = mp.getChildPackages();

		for (MetaPackage childPackage : childPackages)
		{
			walkTree(childPackage, histo, walker);
		}

		List<MetaClass> packageClasses = mp.getPackageClasses();

		for (MetaClass mc : packageClasses)
		{
			for (IMetaMember mm : mc.getMetaMembers())
			{
				walker.processMember(histo, mm);
			}
		}
	}
}
