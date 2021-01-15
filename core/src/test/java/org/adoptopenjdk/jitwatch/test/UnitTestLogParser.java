package org.adoptopenjdk.jitwatch.test;

import java.io.Reader;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.model.CompilerThread;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.parser.AbstractLogParser;

public class UnitTestLogParser extends AbstractLogParser
{
	public UnitTestLogParser(IJITListener jitListener)
	{
		super(jitListener);

		currentCompilerThread = new CompilerThread("1234", "TestCompilerThread");
	}

	@Override
	protected void parseLogFile()
	{
	}

	@Override
	protected void splitLogFile(Reader logFileReader)
	{
	}

	@Override
	protected void handleTag(Tag tag)
	{
	}

	@Override
	public void setTagTaskQueued(Tag tagTaskQueued, IMetaMember metaMember)
	{
		super.setTagTaskQueued(tagTaskQueued, metaMember);
	}

	@Override
	public void setTagNMethod(Tag tagNMethod, IMetaMember member)
	{
		super.setTagNMethod(tagNMethod, member);
	}

	@Override
	public void setTagTask(Task tagTask, IMetaMember member)
	{
		super.setTagTask(tagTask, member);
	}

	public void setTagTaskDone(Tag tagTaskDone, IMetaMember member)
	{
		super.handleTaskDone(tagTaskDone, member);
	}
}