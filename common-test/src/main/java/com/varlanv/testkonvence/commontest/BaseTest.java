package com.varlanv.testkonvence.commontest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface BaseTest {

    String SLOW_TEST_TAG = "slow-test";
    String FAST_TEST_TAG = "fast-test";
    String UNIT_TEST_TAG = "unit-test";
    String INTEGRATION_TEST_TAG = "integration-test";
    String FUNCTIONAL_TEST_TAG = "functional-test";

    @SneakyThrows
    default Path newTempDir() {
        var dir = Files.createTempDirectory("testsyncjunit-");
        dir.toFile().deleteOnExit();
        return dir;
    }

    @SneakyThrows
    default Path newTempFile() {
        var file = Files.createTempFile("testsyncjunit-", "");
        file.toFile().deleteOnExit();
        return file;
    }

    @SneakyThrows
    default void useTempDir(ThrowingConsumer<Path> action) {
        var dir = newTempDir();
        try {
            action.accept(dir);
        } finally {
            FileUtils.deleteDirectory(dir.toFile());
        }
    }

    @SneakyThrows
    default <T> T useTempFile(ThrowingFunction<Path, T> action) {
        var file = newTempFile();
        try {
            return action.apply(file);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @SneakyThrows
    default void consumeTempFile(ThrowingConsumer<Path> action) {
        var file = newTempFile();
        try {
            action.accept(file);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @SneakyThrows
    default void runAndDeleteFile(@NonNull Path file, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } finally {
            if (Files.isRegularFile(file)) {
                Files.deleteIfExists(file);
            } else {
                FileUtils.forceDelete(file.toFile());
            }
        }
    }

    default String java(@Language("Java") String java) {
        return java;
    }

    default String groovy(@Language("Groovy") String groovy) {
        return groovy;
    }

    interface ThrowingRunnable {
        void run() throws Exception;

        default Runnable toUnchecked() {
            return () -> {
                try {
                    run();
                } catch (Exception e) {
                    BaseTest.hide(e);
                }
            };
        }
    }

    interface ThrowingSupplier<T> {
        T get() throws Exception;

        default Supplier<T> toUnchecked() {
            return () -> {
                try {
                    return get();
                } catch (Exception e) {
                    return BaseTest.hide(e);
                }
            };
        }
    }

    interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }

    interface ThrowingPredicate<T> {
        boolean test(T t) throws Exception;

        default Predicate<T> toUnnchecked() {
            return t -> {
                try {
                    return test(t);
                } catch (Exception e) {
                    return hide(e);
                }
            };
        }
    }

    interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable, R> R hide(Throwable t) throws T {
        throw (T) t;
    }

    static <T> T supplyQuiet(ThrowingSupplier<T> supplier) {
        return supplier.toUnchecked().get();
    }

    static void runQuiet(ThrowingRunnable runnable) {
        runnable.toUnchecked().run();
    }
}
