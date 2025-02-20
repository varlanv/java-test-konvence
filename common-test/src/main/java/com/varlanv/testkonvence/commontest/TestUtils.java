package com.varlanv.testkonvence.commontest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {

    private static final AtomicReference<Path> projectRoot = new AtomicReference<>();

    public static Path projectRoot() {
        return projectRoot.get();
    }

    public static void setProjectRoot(Supplier<Path> fileSupplier) {
        if (projectRoot.get() == null) {
            synchronized (TestUtils.class) {
                projectRoot.set(fileSupplier.get());
            }
        }
    }
}

