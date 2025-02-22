package com.varlanv.testkonvence.enforce;

import java.nio.file.Path;
import java.util.List;

public interface SourceFile {

    Path path();

    List<String> lines();

    void save(List<String> lines, String separator);
}
