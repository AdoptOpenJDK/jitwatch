package org.adoptopenjdk.jitwatch.model.assembly.arm;

public enum ARMShifter {
    /*
        All references go to the ARM developer documentation --> https://developer.arm.com/documentation/dui0801/l/Overview-of-AArch64-state/Registers-in-AArch64-state
    */

    LSL("Logical Shift Left"),
    LSR("Logical Shift Right"),
    ASR("Arithmetic Shift Right"),
    ROR("Rotate Right"),
    RRX("Rotate Right with Extend");

    private final String description;

    ARMShifter(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public static boolean isShifter(String shifterInstruction)
    {
        if (shifterInstruction == null) return false;
        String shifterInstructionAsUpper = shifterInstruction.toUpperCase().trim();

        for (ARMShifter shifter : ARMShifter.values())
        {
            if (shifterInstructionAsUpper.startsWith(shifter.name()))
            {
                return true;
            }
        }

        return false;
    }

    public static ARMShifter fromString(String shifterInstruction)
    {
        if (shifterInstruction == null) return null;

        String shifterInstructionAsUpper = shifterInstruction.toUpperCase().trim();
        for (ARMShifter shifter : ARMShifter.values())
        {
            if (shifter.name().equals(shifterInstructionAsUpper)) {
                return shifter;
            }
        }

        return null;
    }
}
