package com.varlanv.testkonvence.commontest.sample;

import com.varlanv.testkonvence.commontest.ImmutableList;
import java.nio.file.Path;
import org.immutables.value.Value;

@Value.Immutable(builder = false)
public interface ConsumableSample {

    @Value.Parameter
    String description();

    @Value.Parameter
    Path dir();

    @Value.Parameter
    ImmutableList<SampleSourceFile> sources();

    @Value.Parameter
    SampleOptions options();

    default SampleSourceFile sourceFile() {
        var sources = sources().value();
        if (sources.size() > 1) {
            throw new IllegalStateException("More than one source file available");
        } else if (sources.isEmpty()) {
            throw new IllegalStateException("No source file available");
        }
        return sources.get(0);
    }
}
