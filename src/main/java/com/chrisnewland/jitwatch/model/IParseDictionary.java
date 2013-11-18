package com.chrisnewland.jitwatch.model;

public interface IParseDictionary
{
	public void setType(String id, Tag type);
	public void setKlass(String id, Tag klass);
	public void setMethod(String id, Tag method);
	
	public Tag getType(String id);
	public Tag getKlass(String id);
	public Tag getMethod(String id);
}