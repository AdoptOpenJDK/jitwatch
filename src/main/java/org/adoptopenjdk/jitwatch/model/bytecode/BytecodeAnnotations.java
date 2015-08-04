package org.adoptopenjdk.jitwatch.model.bytecode;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_BYTECODE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BytecodeAnnotations
{	
	private static final Logger logger = LoggerFactory.getLogger(BytecodeAnnotations.class);

	private Map<Integer, List<LineAnnotation>> annotationMap = new HashMap<>();
	
	public void addAnnotation(int bci, LineAnnotation annotation)
	{
		if (DEBUG_LOGGING_BYTECODE)
		{
			logger.debug("BCI: {} Annotation: {}", bci, annotation.getAnnotation());
		}

		List<LineAnnotation> existingAnnotations = annotationMap.get(bci);

		if (existingAnnotations == null)
		{
			existingAnnotations = new ArrayList<>();
			annotationMap.put(bci, existingAnnotations);
		}

		existingAnnotations.add(annotation);
	}
	
	public List<LineAnnotation> getAnnotationsForBCI(int bci)
	{
		return annotationMap.get(bci);
	}
	
	public boolean hasAnnotationsForBCI(int bci)
	{
		return annotationMap.containsKey(bci);		
	}
	
	public void clear()
	{
		annotationMap.clear();
	}
	
	public int annotatedLineCount()
	{
		return annotationMap.size();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		for (Map.Entry<Integer, List<LineAnnotation>> entry : annotationMap.entrySet())
		{
			for (LineAnnotation annotation : entry.getValue())
			{
				builder.append(entry.getKey()).append(S_SPACE).append(S_COLON).append(S_SPACE).append(annotation.toString()).append(S_NEWLINE);
			}			
		}
		
		return builder.toString();
	}
	
}
