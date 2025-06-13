package com.varlanv.testkonvence.gradle.plugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

interface SourceFile {

    Path path();

    SourceLines lines();

    void save(SourceLines sourceLines) throws Exception;

    static SourceFile ofPath(Path path) throws Exception {
        var sourceLines = SourceLines.ofPath(path);
        return new SourceFile() {

            @Override
            public Path path() {
                return path;
            }

            @Override
            public SourceLines lines() {
                return sourceLines;
            }

            @Override
            public void save(SourceLines sourceLines) throws Exception {
                Files.write(
                        path,
                        sourceLines.joined().getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
        };
    }
}
