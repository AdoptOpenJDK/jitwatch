/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
public class IsUsedForTestingDefaultPackage
{
	// Class in the default package beginning with I (also representation on int parameter)
	// to regression check matching functionality
	
	@Override
	public String toString()
	{
		return "Who watches the watchmen?";
	}
}