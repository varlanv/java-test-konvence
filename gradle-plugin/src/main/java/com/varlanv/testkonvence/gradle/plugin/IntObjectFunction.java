package com.varlanv.testkonvence.gradle.plugin;

@FunctionalInterface
interface IntObjectFunction<T, R> {

    R apply(int i, T t);
}
