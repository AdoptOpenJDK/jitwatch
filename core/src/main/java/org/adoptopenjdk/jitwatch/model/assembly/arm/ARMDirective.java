package org.adoptopenjdk.jitwatch.model.assembly.arm;

public enum ARMDirective {
    INST(".inst"),
    WORD(".word"),
    HWORD(".hword"),
    BYTE(".byte"),
    ALIGN(".align"),
    TEXT(".text"),
    DATA(".data"),
    ASCII(".ascii"),
    ASCIZ(".asciz"),
    SECTION(".section"),
    GLOBAL(".global", ".globl"),
    FUNC(".func"),
    ENDFUNC(".endfunc"),
    THUMB(".thumb"),
    ARM(".arm"),
    LTORG(".ltorg"),
    REQ(".req"),
    CFI(".cfi");

    private final String[] tokens;

    ARMDirective(String... tokens) {
        this.tokens = tokens;
    }

    public static boolean isDirective(String line) {
        if (line == null || line.trim().isEmpty()) return false;

        String trimmed = line.trim();
        for (ARMDirective directive : values()) {
            for (String token : directive.tokens) {
                if (trimmed.startsWith(token + " ") || trimmed.equals(token)) {
                    return true;
                }
            }
        }
        return false;
    }
}
