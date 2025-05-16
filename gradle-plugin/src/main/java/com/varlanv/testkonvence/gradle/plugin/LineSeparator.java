package com.varlanv.testkonvence.gradle.plugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.val;

interface LineSeparator {

    String separator();

    @SneakyThrows
    static LineSeparator forFile(Path path, String text) {
        if (text.isEmpty()) {
            return () -> "";
        }
        val result = new String[1];
        try (val lines = Files.lines(path, StandardCharsets.UTF_8)) {
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

    @SneakyThrows
    static LineSeparator forFile(Path path) {
        return forFile(path, new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
    }
}
