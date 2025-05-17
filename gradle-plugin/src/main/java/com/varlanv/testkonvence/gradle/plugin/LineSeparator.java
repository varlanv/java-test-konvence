package com.varlanv.testkonvence.gradle.plugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

interface LineSeparator {

    String separator();

    static LineSeparator forFile(Path path, String text) throws Exception {
        if (text.isEmpty()) {
            return () -> "";
        }
        var result = new String[1];
        try (var lines = Files.lines(path, StandardCharsets.UTF_8)) {
            lines.limit(1).forEach(line -> {
                if (text.startsWith(line + "\r\n")) {
                    result[0] = "\r\n";
                } else {
                    result[0] = "\n";
                }
            });
        }
        return () -> result[0];
    }

    static LineSeparator forFile(Path path) throws Exception {
        return forFile(path, Files.readString(path, StandardCharsets.UTF_8));
    }
}
