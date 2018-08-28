package org.adoptopenjdk.jitwatch.model;

class ExtractedJITDataModel {
    private String vmVersionRelease;

    ExtractedJITDataModel() {
    }

    void setVmVersionRelease(String release) {
        this.vmVersionRelease = release;
    }

    @Override
    int getJDKMajorVersion() {
        int result = 8; // fallback

        if (this.vmVersionRelease != null) {
            if (this.vmVersionRelease.contains("1.7")) {
                result = 7;
            } else if (this.vmVersionRelease.contains("1.8")) {
                result = 8;
            } else if (this.vmVersionRelease.startsWith("9")) {
                result = 9;
            } else if (this.vmVersionRelease.startsWith("10")) {
                result = 10;
            }
        }

        return result;
    }
}