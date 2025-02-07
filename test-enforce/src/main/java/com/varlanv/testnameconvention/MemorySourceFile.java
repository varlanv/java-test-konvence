package com.varlanv.testnameconvention;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class MemorySourceFile implements SourceFile {

    @Getter
    String path;
    List<String> lines;
    @NonFinal
    @Nullable
    List<String> modifiedLines;

    public MemorySourceFile(String path, @Language("Java") String content) {
        this(path, Arrays.asList(content.split(System.lineSeparator())));
    }

    @Override
    public List<String> lines() {
        return modifiedLines == null ? lines : modifiedLines;
    }

    @Override
    public void save(List<String> lines) {
        modifiedLines = lines;
    }

    @Override
    public String toString() {
        return String.join(System.lineSeparator(), lines());
    }
}
