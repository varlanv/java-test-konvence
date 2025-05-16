package com.varlanv.testkonvence.commontest;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {

    private static final AtomicReference<@Nullable Path> projectRoot = new AtomicReference<>();

    @Nullable public static Path projectRoot() {
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
