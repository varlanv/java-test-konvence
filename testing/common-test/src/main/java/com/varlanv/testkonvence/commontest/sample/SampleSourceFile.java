package com.varlanv.testkonvence.commontest.sample;

import com.varlanv.testkonvence.commontest.BaseTest;
import java.nio.file.Files;
import java.nio.file.Path;
import org.immutables.value.Value;

@Value.Immutable(builder = false)
public interface SampleSourceFile {

    @Value.Parameter
    Path path();

    @Value.Parameter
    String outerClassName();

    @Value.Parameter
    String packageName();

    @Value.Parameter
    String expectedTransformation();

    default String content() {
        return BaseTest.supplyQuiet(() -> Files.readString(path()));
    }
}
