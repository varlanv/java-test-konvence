package com.varlanv.testkonvence;

@FunctionalInterface
public interface ThrowingSupplier<T> {

    T get() throws Exception;
}
