package com.varlanv.testkonvence.gradle.plugin;

import java.util.function.BinaryOperator;
import lombok.experimental.UtilityClass;

@UtilityClass
class FunctionalUtil {

    public static <T> BinaryOperator<T> throwingCombiner() {
        return (t1, t2) -> {
            throw new IllegalStateException("Parallel streams are not supported");
        };
    }
}
