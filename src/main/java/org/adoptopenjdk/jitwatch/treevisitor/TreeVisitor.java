package org.adoptopenjdk.jitwatch.treevisitor;

import java.util.List;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.MetaPackage;

public final class TreeVisitor
{
    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private TreeVisitor() {
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
