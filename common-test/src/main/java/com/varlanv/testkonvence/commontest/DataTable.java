package com.varlanv.testkonvence.commontest;

public record DataTable(Boolean isCi,
                        Boolean configurationCache,
                        Boolean buildCache,
                        String gradleVersion) {
}
