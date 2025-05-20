package org.adoptopenjdk.jitwatch.model.assembly.arm;

public enum ARMRegisterType
    /*
        All references go to the ARM developer documentation --> https://developer.arm.com/documentation/dui0801/l/Overview-of-AArch64-state/Registers-in-AArch64-state
     */
{
    // ARM64 registers
    ARM64_GENERAL("64-bit ARM register", "x\\d+"),
    ARM64_32BIT("32-bit ARM register", "w\\d+"),
    ARM64_VECTOR("ARM vector register", "v\\d+"),
    ARM64_SIMD_B("ARM SIMD byte register", "b\\d+"),
    ARM64_SIMD_H("ARM SIMD half-word register", "h\\d+"),
    ARM64_SIMD_S("ARM SIMD single-word register", "s\\d+"),
    ARM64_SIMD_D("ARM SIMD double-word register", "d\\d+"),
    ARM64_SIMD_Q("ARM SIMD quad-word register", "q\\d+"),

    // ARM32 registers
    ARM32_GENERAL("ARM general purpose register", "r\\d+"),

    // Special registers - already include their purpose in the description
    ARM_SP("stack pointer register", "sp"),
    ARM_LR("link register", "lr"),
    ARM_PC("program counter register", "pc"),
    ARM_ZR("zero register", "(xzr|wzr)"),

    // Default case
    ARM_DEFAULT("ARM register", ".*");

    private final String description;
    private final String pattern;

    ARMRegisterType(String description, String pattern)
    {
        this.description = description;
        this.pattern = pattern;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean matches(String regName)
    {
        return regName != null && regName.matches(pattern);
    }

    public static ARMRegisterType fromRegisterName(String regName)
    {
        if (regName == null) {
            return ARM_DEFAULT;
        }

        for (ARMRegisterType type : values())
        {
            if (type.matches(regName))
            {
                return type;
            }
        }

        return ARM_DEFAULT;
    }

    public static String getSpecialPurpose(String regName)
    {
        if (regName == null) {
            return "";
        }

        // For special registers, we already include this in the description, but we can check additional special cases here
        if (regName.endsWith("fp")) {
            return " (frame pointer)";
        }

        return "";
    }
}