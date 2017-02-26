/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
public class FooClassInDefaultPackage
{
	private int primitiveParam;

	private int[] primitiveArrayParam;
	
	private IsUsedForTestingDefaultPackage classParam;
	
	private IsUsedForTestingDefaultPackage[] classArrayParam;

	public int[] getPrimitiveArrayParam()
	{
		return primitiveArrayParam;
	}

	public void setPrimitiveArrayParam(int[] primitiveArrayParam)
	{
		this.primitiveArrayParam = primitiveArrayParam;
	}

	public void setClassParam(IsUsedForTestingDefaultPackage classParam)
	{
		this.classParam = classParam;
	}

	public IsUsedForTestingDefaultPackage getClassParam()
	{
		return classParam;
	}

	public int getPrimitiveParam()
	{
		return primitiveParam;
	}

	public void setPrimitiveParam(int primitiveParam)
	{
		this.primitiveParam = primitiveParam;
	}

	public IsUsedForTestingDefaultPackage[] getClassArrayParam()
	{
		return classArrayParam;
	}

	public void setClassArrayParam(IsUsedForTestingDefaultPackage[] classArrayParam)
	{
		this.classArrayParam = classArrayParam;
	}
}