package com.varlanv.testkonvence.gradle.plugin;

import java.util.function.BinaryOperator;

class FunctionalUtil {

    private FunctionalUtil() {}

    public static <T> BinaryOperator<T> throwingCombiner() {
        return (t1, t2) -> {
            throw new IllegalStateException("Parallel streams are not supported");
        };
    }
}
