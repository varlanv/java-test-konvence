package com.varlanv.testkonvence.commontest.sample;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Getter
@RequiredArgsConstructor
public class SampleSourceFile {

    Path path;
    String outerClassName;
    String packageName;
    String expectedTransformation;

    @SneakyThrows
    public String content() {
        return Files.readString(path);
    }
}
