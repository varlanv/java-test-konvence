package com.varlanv.testkonvence.gradle.plugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import lombok.SneakyThrows;
import lombok.val;

interface SourceFile {

    Path path();

    SourceLines lines();

    void save(SourceLines sourceLines);

    static SourceFile ofPath(Path path) {
        val sourceLines = SourceLines.ofPath(path);
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
            @SneakyThrows
            public void save(SourceLines sourceLines) {
                Files.write(
                        path,
                        sourceLines.joined().getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
        };
    }
}
