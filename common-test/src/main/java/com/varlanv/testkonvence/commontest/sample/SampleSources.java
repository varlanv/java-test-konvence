package com.varlanv.testkonvence.commontest.sample;

import org.immutables.value.Value;

@Value.Immutable(builder = false)
public interface SampleSources {

    @Value.Parameter
    String outerClassName();

    @Value.Parameter
    String fileName();

    @Value.Parameter
    String packageName();

    @Value.Parameter
    String sources();

    @Value.Parameter
    String expectedTransformation();
}
