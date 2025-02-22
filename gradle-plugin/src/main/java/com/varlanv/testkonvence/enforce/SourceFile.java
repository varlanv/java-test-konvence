package com.varlanv.testkonvence.enforce;

import java.nio.file.Path;
import java.util.List;

public interface SourceFile {

    Path path();

    List<String> lines();

    String text();

    void save(String text);

    void save(List<String> lines, String separator);
}
