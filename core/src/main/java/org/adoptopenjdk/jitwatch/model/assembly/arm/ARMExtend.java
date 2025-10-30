package org.adoptopenjdk.jitwatch.model.assembly.arm;

public enum ARMExtend
{
    SXTW("Sign-Extend Word"),
    SXTB("Sign-Extend Byte"),
    SXTH("Sign-Extend Halfword"),
    UXTB("Zero-Extend Byte"),
    UXTH("Zero-Extend Halfword"),
    UXTW("Zero-Extend Word");

    private final String description;

    ARMExtend(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public static boolean isExtend(String extendInstruction)
    {
        if (extendInstruction == null) return false;

        String extendInstructionMnemonic = extendInstruction.trim().split("\\s+")[0].toUpperCase();

        for (ARMExtend armExtend : ARMExtend.values())
        {
            if (armExtend.name().equals(extendInstructionMnemonic)) {
                return true;
            }
        }

        return false;
    }

    public static ARMExtend fromString(String extendInstruction)
    {
        if (extendInstruction == null) return null;

        String extendInstructionMnemonic = extendInstruction.trim().split("\\s+")[0].toUpperCase();

        for (ARMExtend armExtend : ARMExtend.values())
        {
            if (armExtend.name().equals(extendInstructionMnemonic)) {
                return armExtend;
            }
        }

        return null;
    }
}
