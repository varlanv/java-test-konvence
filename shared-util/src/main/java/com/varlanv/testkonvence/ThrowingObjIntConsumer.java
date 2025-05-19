package com.varlanv.testkonvence;

@FunctionalInterface
public interface ThrowingObjIntConsumer<T> {

    void accept(T t, int value) throws Exception;
}
