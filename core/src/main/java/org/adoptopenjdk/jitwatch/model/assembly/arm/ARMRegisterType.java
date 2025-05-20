package org.adoptopenjdk.jitwatch.model.assembly.arm;

public enum ARMRegisterType
    /*
        All references go to the ARM developer documentation --> https://developer.arm.com/documentation/dui0801/l/Overview-of-AArch64-state/Registers-in-AArch64-state
     */
{
    ARM64_GENERAL("64-bit ARM register", "x\\d+"),
    ARM64_32BIT("32-bit ARM register", "w\\d+"),
    ARM64_VECTOR("ARM vector register", "v\\d+"),
    ARM64_SIMD_B("ARM SIMD byte register", "b\\d+"),
    ARM64_SIMD_H("ARM SIMD half-word register", "h\\d+"),
    ARM64_SIMD_S("ARM SIMD single-word register", "s\\d+"),
    ARM64_SIMD_D("ARM SIMD double-word register", "d\\d+"),
    ARM64_SIMD_Q("ARM SIMD quad-word register", "q\\d+"),
    ARM32_GENERAL("ARM general purpose register", "r\\d+"),

    ARM_SP("stack pointer", "sp"),
    ARM_LR("link register", "lr"),
    ARM_PC("program counter", "pc"),
    ARM_ZR("zero register", "(xzr|wzr)");

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
        for (ARMRegisterType type : values())
        {
            if (type.matches(regName))
            {
                return type;
            }
        }

        return null;
    }

    public String getSpecialPurpose(String regName)
    {
        if (regName.equals("sp") || regName.endsWith("sp"))
        {
            return " (stack pointer)";
        }
        else if (regName.equals("lr"))
        {
            return " (link register)";
        }
        else if (regName.equals("pc"))
        {
            return " (program counter)";
        }
        else if (regName.endsWith("bp") || regName.endsWith("fp"))
        {
            return " (frame pointer)";
        }
        else if (regName.endsWith("ax"))
        {
            return " (accumulator)";
        }

        return "";
    }
}
