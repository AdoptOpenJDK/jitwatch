package org.adoptopenjdk.jitwatch.sandbox;

public interface IExternalProcess
{
	public String getOutputStream();
	
	public String getErrorStream();
}