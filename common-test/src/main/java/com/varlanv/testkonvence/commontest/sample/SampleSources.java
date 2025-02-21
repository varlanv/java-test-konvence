package com.varlanv.testkonvence.commontest.sample;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SampleSources {

    String outerClassName;
    String fileName;
    String packageName;
    String sources;
    String expectedTransformation;
}