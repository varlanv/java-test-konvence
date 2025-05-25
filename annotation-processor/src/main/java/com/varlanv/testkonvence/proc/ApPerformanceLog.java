package com.varlanv.testkonvence.proc;

import com.varlanv.testkonvence.ThrowingConsumer;
import com.varlanv.testkonvence.ThrowingRunnable;
import java.time.Duration;
import java.util.function.Consumer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

final class ApPerformanceLog {

    private long totalTime;
    private final ThrowingConsumer<ThrowingRunnable> incrementTotal;
    private final Consumer<Runnable> incrementTotalSafe;
    private final Consumer<ProcessingEnvironment> printResult;

    ApPerformanceLog(boolean enabled) {
        totalTime = 0;
        if (enabled) {
            incrementTotal = action -> {
                var before = System.currentTimeMillis();
                try {
                    action.run();
                } finally {
                    var after = System.currentTimeMillis();
                    totalTime += after - before;
                }
            };
            incrementTotalSafe = action -> {
                var before = System.currentTimeMillis();
                try {
                    action.run();
                } finally {
                    var after = System.currentTimeMillis();
                    totalTime += after - before;
                }
            };
            printResult = processingEnvironment -> {
                processingEnvironment
                        .getMessager()
                        .printMessage(
                                Diagnostic.Kind.NOTE,
                                "Total time for TestKonvence annotation processing - " + Duration.ofMillis(totalTime));
            };
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
