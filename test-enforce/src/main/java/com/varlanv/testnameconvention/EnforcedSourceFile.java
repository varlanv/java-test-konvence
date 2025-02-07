package com.varlanv.testnameconvention;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class EnforcedSourceFile implements SourceFile {

    String path;

    @Override
    @SneakyThrows
    public List<String> lines() {
        return Files.readAllLines(Paths.get(path));
    }

    @Override
    @SneakyThrows
    public void save(List<String> lines) {
        Files.write(Paths.get(path), lines);
    }
}
