package org.adoptopenjdk.jitwatch.model;

import java.util.Map;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class XMLStringBuilder {
    private static final String INDENT = "  ";

    public static String buildXMLString(Tag tag, boolean showChildren) {
        StringBuilder builder = new StringBuilder();

        int myDepth = tag.getDepth();

        for (int i = 0; i < myDepth; i++) {
            builder.append(INDENT);
        }

        builder.append(C_OPEN_ANGLE).append(tag.getName());

        Map<String, String> attrs = tag.getAttributes();

        if (attrs.size() > 0) {
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                builder.append(C_SPACE).append(entry.getKey()).append(C_EQUALS).append(C_DOUBLE_QUOTE);
                builder.append(entry.getValue()).append(C_DOUBLE_QUOTE);
            }
        }

        if (tag.isSelfClosing()) {
            builder.append(C_SLASH).append(C_CLOSE_ANGLE).append(C_NEWLINE);
        } else {
            if (showChildren && tag.getChildren().size() > 0) {
                builder.append(C_CLOSE_ANGLE).append(C_NEWLINE);

                for (Tag child : tag.getChildren()) {
                    builder.append(buildXMLString(child, true));
                }
            } else {
                builder.append(C_CLOSE_ANGLE).append(C_NEWLINE);

                if (tag.getTextContent() != null) {
                    for (int i = 0; i < myDepth; i++) {
                        builder.append(INDENT);
                    }

                    builder.append(tag.getTextContent()).append(C_NEWLINE);
                }
            }

            for (int i = 0; i < myDepth; i++) {
                builder.append(INDENT);
            }

            builder.append(C_OPEN_ANGLE).append(C_SLASH);
            builder.append(tag.getName()).append(C_CLOSE_ANGLE).append(C_NEWLINE);
        }

        return builder.toString();
    }
}
