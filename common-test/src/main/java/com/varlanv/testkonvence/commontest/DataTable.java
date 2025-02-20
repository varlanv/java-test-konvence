package com.varlanv.testkonvence.commontest;

public record DataTable(boolean isCi,
                        boolean configurationCache,
                        Boolean buildCache,
                        String gradleVersion) {
}
