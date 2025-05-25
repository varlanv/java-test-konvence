package com.varlanv.testkonvence;

@FunctionalInterface
public interface ThrowingConsumer<T> {

    void accept(T t) throws Exception;
}
