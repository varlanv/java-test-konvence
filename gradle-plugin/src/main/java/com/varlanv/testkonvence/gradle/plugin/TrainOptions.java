package com.varlanv.testkonvence.gradle.plugin;

import org.immutables.value.Value;

@Value.Immutable
interface TrainOptions {

    @Value.Default
    default boolean dryWithFailing() {
        return false;
    }

    @Value.Default
    default boolean reverseTransformation() {
        return false;
    }

    @Value.Default
    default boolean camelCaseMethodName() {
        return false;
    }
}
