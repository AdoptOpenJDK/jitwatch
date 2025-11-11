package org.adoptopenjdk.jitwatch.model.assembly.x86;

public enum X86RegisterType
{
    REG_32BIT("32-bit register", "e", true, null),
    REG_64BIT("64-bit register", "r", true, null),
    REG_DEBUG("debug register", "db", true, null),
    REG_CONTROL("processor control register", "cr", true, null),
    REG_TEST("test register", "tr", true, null),
    REG_FP_STACK("floating point register stack", "st", true, null),
    REG_MMX("MMX register", "mm", true, null),
    REG_SSE("SSE register", "xmm", true, null),
    REG_SECTION("section register", "s", false, null), // suffix match

    // Special purposes (these are special cases checked after primary matching)
    REG_ACCUMULATOR(null, "ax", false, "accumulator"),
    REG_FRAME_POINTER(null, "bp", false, "frame pointer"),
    REG_STACK_POINTER(null, "sp", false, "stack pointer"),

    REG_DEFAULT("Register", null, false, null);

    private final String description;
    private final String pattern;
    private final boolean isPrefix;
    private final String specialPurpose;

    X86RegisterType(String description, String pattern, boolean isPrefix, String specialPurpose) {
        this.description = description;
        this.pattern = pattern;
        this.isPrefix = isPrefix;
        this.specialPurpose = specialPurpose;
    }

    public static X86RegisterType fromRegisterName(String regName) {
        if (regName == null || regName.isEmpty()) {
            return REG_DEFAULT;
        }

        // First check for primary register types (based on prefix/suffix)
        for (X86RegisterType type : values()) {
            if (type.pattern != null &&
                    ((type.isPrefix && regName.startsWith(type.pattern)) ||
                            (!type.isPrefix && regName.endsWith(type.pattern)))) {
                // Skip special purpose entries (those without a description)
                if (type.description != null) {
                    return type;
                }
            }
        }

        return REG_DEFAULT;
    }

    public static String getSpecialPurpose(String regName) {
        if (regName == null || regName.isEmpty()) {
            return "";
        }

        for (X86RegisterType type : values()) {
            if (type.specialPurpose != null &&
                    type.pattern != null &&
                    (!type.isPrefix && regName.endsWith(type.pattern))) {
                return " (" + type.specialPurpose + ")";
            }
        }

        return "";
    }

    public String getDescription() {
        return description;
    }
}
