package com.varlanv.testnameconvention;

import java.util.List;

public interface SourceFile {

    String path();

    List<String> lines();

    void save(List<String> lines);
}
