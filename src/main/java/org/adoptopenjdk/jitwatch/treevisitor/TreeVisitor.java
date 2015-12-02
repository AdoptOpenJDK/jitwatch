/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.treevisitor;

import java.util.List;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.MetaPackage;

public final class TreeVisitor
{
	private TreeVisitor()
	{
	}

	public static void walkTree(IReadOnlyJITDataModel model, ITreeVisitable visitable)
	{
		visitable.reset();

		List<MetaPackage> roots = model.getPackageManager().getRootPackages();

		for (MetaPackage mp : roots)
		{
			walkPackage(mp, visitable);
		}
	}

	private static void walkPackage(MetaPackage mp, ITreeVisitable visitable)
	{
		List<MetaPackage> childPackages = mp.getChildPackages();

		for (MetaPackage childPackage : childPackages)
		{
			walkPackage(childPackage, visitable);
		}

		List<MetaClass> packageClasses = mp.getPackageClasses();

		for (MetaClass mc : packageClasses)
		{
			for (IMetaMember mm : mc.getMetaMembers())
			{
				visitable.visit(mm);
			}
		}
	}
}
