package org.adoptopenjdk.jitwatch.model.assembly.x86;

public enum X86Directive {
    DB(".db", "db"),
    DW(".dw", "dw"),
    DD(".dd", "dd"),
    DQ(".dq", "dq"),
    BYTE(".byte"),
    WORD(".word"),
    LONG(".long"),
    QUAD(".quad"),
    ASCII(".ascii"),
    ASCIZ(".asciz"),
    STRING(".string"),
    TEXT(".text"),
    DATA(".data"),
    BSS(".bss"),
    SECTION(".section"),
    GLOBAL(".global", ".globl"),
    ALIGN(".align"),
    EQU(".equ"),
    SET(".set"),
    EXTERN(".extern"),
    TYPE(".type"),
    SIZE(".size"),
    FILE(".file"),
    LOC(".loc"),
    CFI(".cfi");

    private final String[] tokens;

    X86Directive(String... tokens) {
        this.tokens = tokens;
    }

    public static boolean isDirective(String line) {
        if (line == null || line.trim().isEmpty()) return false;

        String trimmed = line.trim();
        for (X86Directive directive : values()) {
            for (String token : directive.tokens) {
                if (trimmed.startsWith(token + " ") || trimmed.equals(token)) {
                    return true;
                }
            }
        }
        return false;
    }
}