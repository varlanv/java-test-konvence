package com.varlanv.testkonvence;

@FunctionalInterface
public interface IntObjectFunction<T, R> {

    R apply(int i, T t);
}
