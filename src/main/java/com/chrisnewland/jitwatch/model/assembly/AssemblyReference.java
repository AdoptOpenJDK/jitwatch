/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model.assembly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AssemblyReference
{
	private static Map<String, String> mnemonicMap = new HashMap<>();
	
	private static final String TAG_MNEM_OPEN = "<mnem>";
	private static final String TAG_MNEM_CLOSE = "</mnem>";
	private static final String TAG_BRIEF_OPEN = "<brief>";
	private static final String TAG_BRIEF_CLOSE = "</brief>";
    private static final int INPUT_BUFFER_SIZE = 65536;

    static
	{
		try
		{
			BufferedReader input = new BufferedReader(
                    new InputStreamReader(
                            AssemblyReference.class.getResourceAsStream("/x86reference.xml")),
                    INPUT_BUFFER_SIZE);

			String currentLine = input.readLine();

			Set<String> mnemonics = new HashSet<>();
			
			while (currentLine != null)
			{
				int openMnemPos = currentLine.indexOf(TAG_MNEM_OPEN);
				
				if (openMnemPos != -1)
				{
					int closeMnemPos = currentLine.indexOf(TAG_MNEM_CLOSE);
					
					if (closeMnemPos != -1)
					{
						String mnemonic = currentLine.substring(openMnemPos + TAG_MNEM_OPEN.length(), closeMnemPos).toLowerCase();
						
						mnemonics.add(mnemonic);
					}					
				}
				
				int openBriefPos = currentLine.indexOf(TAG_BRIEF_OPEN);
				
				if (openBriefPos != -1)
				{
					int closeBriefPos = currentLine.indexOf(TAG_BRIEF_CLOSE);
					
					if (closeBriefPos != -1)
					{
						String brief = currentLine.substring(openBriefPos + TAG_BRIEF_OPEN.length(), closeBriefPos);
						
						for (String mnemonic : mnemonics)
						{
							if (!mnemonicMap.containsKey(mnemonic))
							{
								mnemonicMap.put(mnemonic, brief);
							}
						}
						
						mnemonics.clear();
					}					
				}				

				currentLine = input.readLine();
			}

			input.close();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		
		// patch up missing descriptions
		mnemonicMap.put("movabs", "Move a 64-bit value");
		mnemonicMap.put("ret", mnemonicMap.get("retn"));
		mnemonicMap.put("movslq", mnemonicMap.get("movsxd"));

	}

	private AssemblyReference()
	{
	}

	public static String lookupMnemonic(String mnemonic)
	{
		String result = mnemonicMap.get(mnemonic);
		
		if (result == null)
		{
			// check if it has a size suffix (b,w,l,q)
			if (mnemonic.endsWith("b") || mnemonic.endsWith("w") ||mnemonic.endsWith("l") ||mnemonic.endsWith("q"))
			{
				result = mnemonicMap.get(mnemonic.substring(0, mnemonic.length()-1));
			}
		}
		
		return result;
	}
}