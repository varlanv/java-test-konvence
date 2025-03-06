package com.varlanv.testkonvence.gradle.plugin;

import java.util.List;

interface Source {

    SourceFile file();

    List<String> lines();

    static Source of(SourceFile file, List<String> lines) {
        return new Source() {
            @Override
            public SourceFile file() {
                return file;
            }

            @Override
            public List<String> lines() {
                return lines;
            }
        };
    }
}
