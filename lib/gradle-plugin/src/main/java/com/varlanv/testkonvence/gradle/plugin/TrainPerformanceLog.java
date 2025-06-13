package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.*;
import java.time.Duration;
import java.util.function.Supplier;
import org.slf4j.Logger;

final class TrainPerformanceLog {

    private long total;
    private final ThrowingConsumer<ThrowingRunnable> totalConsumer;
    private final ThrowingBiFunction<Supplier<String>, ThrowingSupplier<?>, ?> intermediateSupplierFunction;
    private final ThrowingBiConsumer<Supplier<String>, ThrowingRunnable> intermediateRunnableFunction;
    private final Runnable printResul;

    TrainPerformanceLog(boolean enabled, Logger log) {
        total = 0;
        if (enabled) {
            totalConsumer = action -> {
                var before = System.currentTimeMillis();
                try {
                    action.run();
                } finally {
                    var after = System.currentTimeMillis();
                    total += after - before;
                }
            };
            intermediateSupplierFunction = (messageSupplier, action) -> {
                var before = System.currentTimeMillis();
                try {
                    return action.get();
                } finally {
                    var resultTime = System.currentTimeMillis() - before;
                    log.error(
                            "Finished supplier action in {} - {}",
                            Duration.ofMillis(resultTime),
                            messageSupplier.get());
                }
            };
            intermediateRunnableFunction = (messageSupplier, action) -> {
                var before = System.currentTimeMillis();
                try {
                    action.run();
                } finally {
                    var resultTime = System.currentTimeMillis() - before;
                    log.error(
                            "Finished runnable action in {} - {}",
                            Duration.ofMillis(resultTime),
                            messageSupplier.get());
                }
            };
            printResul = () -> log.error("Total time for TestKonvence action - {}", Duration.ofMillis(total));
        } else {
            totalConsumer = ThrowingRunnable::run;
            intermediateSupplierFunction = (messageSupplier, action) -> action.get();
            intermediateRunnableFunction = (messageSupplier, action) -> action.run();
            printResul = () -> {};
        }
    }

    void acceptTotal(ThrowingRunnable action) throws Exception {
        totalConsumer.accept(action);
    }

    void printResult() {
        printResul.run();
    }

    <T> T printIntermediateSupplier(Supplier<String> message, ThrowingSupplier<T> supplier) throws Exception {
        @SuppressWarnings("unchecked")
        var result = (T) intermediateSupplierFunction.apply(message, supplier);
        return result;
    }

    void printIntermediateRunnable(Supplier<String> message, ThrowingRunnable runnable) throws Exception {
        intermediateRunnableFunction.accept(message, runnable);
    }
}
