package com.varlanv.testkonvence.gradle.plugin;

import lombok.SneakyThrows;
import lombok.val;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

interface SourceLines {

    SourceLines replaceAt(int idx, Function<String, String> newLine);

    SourceLines pushAbove(int idx, String newLine);

    List<String> view();

    boolean changed();

    String joined();

    @SneakyThrows
    static SourceLines ofPath(Path path) {
        val originalText = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        val lineSeparator = LineSeparator.forFile(path, originalText).separator();
        val lines = originalText.split(lineSeparator, -1);
        val view = Collections.unmodifiableList(Arrays.asList(lines));
        val hasChanges = new boolean[]{false};

        return new SourceLines() {

            @Override
            public SourceLines replaceAt(int idx, Function<String, String> newLine) {
                val changedLine = newLine.apply(lines[idx]);
                if (!Objects.equals(lines[idx], changedLine)) {
                    lines[idx] = changedLine;
                    hasChanges[0] = true;
                }
                return this;
            }

            @Override
            public SourceLines pushAbove(int idx, String newLine) {
                lines[idx] = newLine + lineSeparator + lines[idx];
                hasChanges[0] = true;
                return this;
            }

            @Override
            public List<String> view() {
                return view;
            }

            @Override
            public boolean changed() {
                return hasChanges[0];
            }

            @Override
            public String joined() {
                if (!changed()) {
                    return originalText;
                }
                return String.join(lineSeparator, lines);
            }
        };
    }
}
