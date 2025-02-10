package com.varlanv.testkonvence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class MemorySourceFile implements SourceFile {

    @Getter
    Path path;
    List<String> lines;
    @NonFinal
    @Nullable
    List<String> modifiedLines;

    public MemorySourceFile(Path path, @Language("Java") String content) {
        this(path, Arrays.asList(content.split(System.lineSeparator())));
    }

    @Override
    public List<String> lines() {
        return modifiedLines == null ? lines : modifiedLines;
    }

    @Override
    public void save(List<String> lines, String separator) {
        modifiedLines = lines;
    }

    @Override
    public String toString() {
        return String.join(System.lineSeparator(), lines());
    }
}
