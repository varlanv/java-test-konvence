package com.varlanv.testkonvence.commontest.sample;

import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConsumableSample {

    String description;
    Path dir;
    List<SampleSourceFile> sources;
    SampleOptions options;

    public SampleSourceFile sourceFile() {
        if (sources.size() > 1) {
            throw new IllegalStateException("More than one source file available");
        } else if (sources.isEmpty()) {
            throw new IllegalStateException("No source file available");
        }
        return sources.get(0);
    }
}
