package com.varlanv.testkonvence;

import java.util.function.BinaryOperator;

public final class FunctionalUtil {

    private FunctionalUtil() {}

    public static <T> BinaryOperator<T> throwingCombiner() {
        return (t1, t2) -> {
            throw new UnsupportedOperationException("Action not supported");
        };
    }
}
