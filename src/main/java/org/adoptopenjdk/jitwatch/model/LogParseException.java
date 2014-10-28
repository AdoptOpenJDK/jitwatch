package org.adoptopenjdk.jitwatch.model;

public class LogParseException extends Exception
{
	private static final long serialVersionUID = 1L;

	public LogParseException(String message)
	{
		super(message);
	}
	
	public LogParseException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
