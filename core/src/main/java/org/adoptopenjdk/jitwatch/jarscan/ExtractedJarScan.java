package org.adoptopenjdk.jitwatch.jarscan;

import java.util.ArrayList;
import java.util.List;

class ExtractedJarScan {
    private List<String> allowedPackagePrefixes = new ArrayList<String>();

    ExtractedJarScan() {
    }

    void addAllowedPackagePrefix(String prefix) {
        allowedPackagePrefixes.add(prefix);
    }

    boolean isAllowedPackage(String fqClassName) {
        boolean allowed = false;

        if (allowedPackagePrefixes.size() == 0) {
            allowed = true;
        } else {
            for (String allowedPrefix : allowedPackagePrefixes) {
                if (fqClassName.startsWith(allowedPrefix)) {
                    allowed = true;
                    break;
                }
            }
        }

        return allowed;
    }
}