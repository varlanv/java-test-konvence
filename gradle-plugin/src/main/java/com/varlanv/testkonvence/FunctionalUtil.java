package com.varlanv.testkonvence;

import lombok.experimental.UtilityClass;

import java.util.function.BinaryOperator;

@UtilityClass
public class FunctionalUtil {

    public static <T> BinaryOperator<T> throwingCombiner() {
        return (t1, t2) -> {
            throw new IllegalStateException("Parallel streams are not supported");
        };
    }
}
