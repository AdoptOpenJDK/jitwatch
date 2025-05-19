/*
 * Copyright (c) 2013-2021 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser; 
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.xml.sax.helpers.DefaultHandler;

import com.chrisnewland.freelogj.Logger;
import com.chrisnewland.freelogj.LoggerFactory;

public final class AssemblyReference
{
	private static Map<String, String> mnemonicMap = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(AssemblyReference.class);

	protected static String ASM_REF_PATH = "/x86reference.xml"; // protected so that AssemblyUtil can change the XML path based on the detected system architecture

	static
	{
		try
		{
			InputStream asmRefInputStream = AssemblyReference.class.getResourceAsStream(ASM_REF_PATH);

			if (asmRefInputStream == null)
			{
				LOGGER.error(
						"Could not find assembly reference {}. If launching from an IDE please add /src/main/resources to your classpath",
						ASM_REF_PATH);
			}
			else
			{
				// SAX parser to anticipate for better XML parsing and less pressure on the GC 
				SAXParserFactory assemblyRefFactory = SAXParserFactory.newInstance(); 
				SAXParser xmlparser = assemblyRefFactory.newSAXParser();
				
				AssemblyReferenceHandler handler = new AssemblyReferenceHandler(); 
				xmlparser.parse(asmRefInputStream, handler);
				
				mnemonicMap = handler.getMnemonicMap();

			}
		}
		catch (IOException ioe)
		{
			LOGGER.error("Could not load assembly reference", ioe);
		}
		catch (ParserConfigurationException pce)
		{ 
			LOGGER.error("The XML Parser suffered a malformed configuration when trying to load in the assembly reference", pce);
		}
		catch (SAXException saxe)
		{ 
			LOGGER.error("The SAX XML Parser failed when trying to load in the assembly reference", saxe);
		}

		// patch up missing descriptions
		mnemonicMap.put("movabs", "Move a 64-bit value");
		mnemonicMap.put("ret", mnemonicMap.get("retn"));
		mnemonicMap.put("movslq", mnemonicMap.get("movsxd"));

	}

	private AssemblyReference()
	{
	}

	private static class AssemblyReferenceHandler extends DefaultHandler
	/* 
	 * SAX is an event-driven system so we need a Handler that can help send these events  
	 * 
	 */
	{
		private final Map<String, String> resultMap = new HashMap<>(); 
		private final Set<String> currentMnemonics = new HashSet<>(); 
		private final StringBuilder txtBuffer = new StringBuilder();

		private boolean insideMnem = false;
		private boolean insideBrief = false;

		@Override
		public void startElement(String uri, String localname, String qname, Attributes attributes)
		/* 
		 * Overriden from the DefaultHandler class of the SAX XML API in Java that acts as you could say 
		 * a "message" to notify Java that we want to start parsing a specific part of the XML, which, for now, 
		 * seems to be the <mnem> and <brief> tags.
		 * 
		 */
		{ 
			if ("mnem".equalsIgnoreCase(qname))
			{
				insideMnem = true; 
				txtBuffer.setLength(0);
			} 
			else if ("brief".equalsIgnoreCase(qname))
			{ 
				insideBrief = true; 
				txtBuffer.setLength(0);
			}
		}

		@Override
		public void characters(char[] character, int start, int length)
		{ 
			txtBuffer.append(character, start, length);
		}

		@Override
		public void endElement(String uri, String localname, String qname)
		/* 
		 * Overriden from the DefaultHandler class of the SAX XML API in Java that acts as you could say 
		 * a "message" to notify Java that we want to end parsing a specific part of the XML, which, for now, 
		 * only be the <mnem> and <brief> tags.
		 * 
		 */
		{ 
			if ("mnem".equalsIgnoreCase(qname))
			{ 
				currentMnemonics.add(txtBuffer.toString().trim().toLowerCase());
				insideMnem = false; 
			} 
			else if ("brief".equalsIgnoreCase(qname))
			{ 
				String brief = txtBuffer.toString().trim();
				for (String mnemonic : currentMnemonics)
				{ 
					mnemonicMap.computeIfAbsent(mnemonic, k -> brief);
				}

				currentMnemonics.clear();
				insideBrief = false; 
			}
		}

		public Map<String, String> getMnemonicMap() 
		{ 
			return mnemonicMap;
		}
	}
	
	public static String lookupMnemonic(String mnemonic)
	{
		String result = mnemonicMap.get(mnemonic);

		if (result == null)
		{
			// check if it has a size suffix (b,w,l,q)
			if (mnemonic.endsWith("b") || mnemonic.endsWith("w") || mnemonic.endsWith("l") || mnemonic.endsWith("q"))
			{
				result = mnemonicMap.get(mnemonic.substring(0, mnemonic.length() - 1));
			}
		}

		return result;
	}
}
