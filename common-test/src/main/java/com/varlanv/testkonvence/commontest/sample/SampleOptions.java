package com.varlanv.testkonvence.commontest.sample;

import org.immutables.value.Value;

@Value.Immutable
public interface SampleOptions {

    @Value.Default
    default boolean camelMethodName() {
        return false;
    }

    @Value.Default
    default boolean reverseTransformation() {
        return false;
    }

    @Value.Default
    default boolean applyAutomaticallyAfterTestTask() {
        return true;
    }
}
