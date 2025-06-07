package org.adoptopenjdk.jitwatch.model.assembly.arm;

import java.util.List;
import java.util.regex.Pattern;

public class MnemonicInfo {
    public final String brief;
    public final List<Pattern> patterns;

    public MnemonicInfo(String brief, List<Pattern> patterns) {
        this.brief = brief;
        this.patterns = patterns;
    }
}
