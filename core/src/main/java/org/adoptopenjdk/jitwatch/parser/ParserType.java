package org.adoptopenjdk.jitwatch.parser;

public enum ParserType
{
	HOTSPOT, J9, ZING;

	public String getDisplayName()
	{
		switch (this)
		{
		case HOTSPOT:
			return "HotSpot";
		case J9:
			return "J9";
		case ZING:
			return "Zing";
		default:
			throw new RuntimeException("Unknown parser type");
		}
	}

	public static ParserType fromString(String input)
	{
		if (input != null)
		{
			switch (input.toLowerCase())
			{
			case "hotspot":
				return HOTSPOT;
			case "j9":
				return J9;
			case "zing":
				return ZING;
			}
		}
		
		throw new RuntimeException("Unknown parser type: " + input);
	}
}