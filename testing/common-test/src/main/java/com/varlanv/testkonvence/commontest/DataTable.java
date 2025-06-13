package com.varlanv.testkonvence.commontest;

public record DataTable(Boolean isCi, Boolean configurationCache, Boolean buildCache, String gradleVersion) {

    public DataTable() {
        this(false, false, false, TestGradleVersions.current());
    }
}
