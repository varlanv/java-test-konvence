package com.varlanv.testkonvence.enforce;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class EnforcedSourceFile implements SourceFile {

    Path path;

    @Override
    @SneakyThrows
    public List<String> lines() {
        return Files.readAllLines(path);
    }

    @Override
    @SneakyThrows
    public void save(List<String> lines, String separator) {
        Files.write(
            path,
            lines.stream().map(it -> it + separator).collect(Collectors.joining()).getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
