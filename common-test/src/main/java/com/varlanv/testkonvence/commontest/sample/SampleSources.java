package com.varlanv.testkonvence.commontest.sample;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SampleSources {

    private final String outerClassName;
    private final String fileName;
    private final String packageName;
    private final String sources;
    private final String expectedTransformation;
}
