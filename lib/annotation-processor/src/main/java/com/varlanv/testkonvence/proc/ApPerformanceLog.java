package com.varlanv.testkonvence.proc;

import com.varlanv.testkonvence.ThrowingConsumer;
import com.varlanv.testkonvence.ThrowingRunnable;
import java.time.Duration;
import java.util.function.Consumer;
import javax.annotation.processing.ProcessingEnvironment;

final class ApPerformanceLog {

    private long totalTime;
    private final ThrowingConsumer<ThrowingRunnable> incrementTotal;
    private final Consumer<Runnable> incrementTotalSafe;
    private final Consumer<ProcessingEnvironment> printResult;

    ApPerformanceLog(boolean enabled) {
        if (enabled) {
            incrementTotal = action -> {
                var before = System.nanoTime();
                try {
                    action.run();
                } finally {
                    var after = System.nanoTime();
                    totalTime += after - before;
                }
            };
            incrementTotalSafe = action -> {
                var before = System.nanoTime();
                try {
                    action.run();
                } finally {
                    var after = System.nanoTime();
                    totalTime += after - before;
                }
            };
            printResult = processingEnvironment -> System.err.println(
                    "Total time for TestKonvence annotation processing - " + Duration.ofNanos(totalTime));
        } else {
            incrementTotal = ThrowingRunnable::run;
            incrementTotalSafe = Runnable::run;
            printResult = processingEnvironment -> {};
        }
    }

    void incrementTotal(ThrowingRunnable action) throws Exception {
        incrementTotal.accept(action);
    }

    void incrementTotalSafe(Runnable action) {
        incrementTotalSafe.accept(action);
    }

    void printResult(ProcessingEnvironment processingEnvironment) {
        printResult.accept(processingEnvironment);
    }
}
