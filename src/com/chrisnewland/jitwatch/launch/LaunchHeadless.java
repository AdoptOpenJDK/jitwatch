package com.chrisnewland.jitwatch.launch;

import java.io.File;
import java.io.IOException;

import com.chrisnewland.jitwatch.core.IJITListener;
import com.chrisnewland.jitwatch.core.JITEvent;
import com.chrisnewland.jitwatch.core.JITWatch;

public class LaunchHeadless
{
    public static void main(String[] args) throws IOException
    {
        JITWatch jw = new JITWatch(new IJITListener()
        {
            @Override
            public void handleLogEntry(String entry)
            {
                System.out.println(entry);
            }
            
            @Override
            public void handleErrorEntry(String entry)
            {
                System.err.println(entry);
            }

			@Override
			public void handleJITEvent(JITEvent event)
			{
                System.out.println(event.toString());				
			}
        });

        //jw.setSuppressMissingClassWarnings(true);
        
        if (args.length != 1)
        {
            System.err.println("Usage: LaunchHeadless <hotspot log file>");
            System.exit(-1);
        }
        
        jw.watch(new File(args[0]));
    }
}
