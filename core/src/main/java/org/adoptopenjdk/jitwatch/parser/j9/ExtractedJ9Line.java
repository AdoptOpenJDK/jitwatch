package org.adoptopenjdk.jitwatch.parser.j9;

import java.util.HashMap;
import java.util.Map;

class ExtractedJ9Line {
    private Map<String, String> attributes = new HashMap<String, String>();

    ExtractedJ9Line() {
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    void addAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    int getBytecodeSize() {
        int result = 0;

        String bcszAttr = attributes.get("bcsz");

        if (bcszAttr != null) {
            try {
                result = Integer.parseInt(bcszAttr);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace(); // TODO log
            }
        }

        return result;
    }
}