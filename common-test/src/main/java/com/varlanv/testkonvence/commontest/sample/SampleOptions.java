package com.varlanv.testkonvence.commontest.sample;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SampleOptions {

    @Builder.Default
    Boolean camelMethodName = false;
    @Builder.Default
    Boolean reverseTransformation = false;
    @Builder.Default
    Boolean applyAutomaticallyAfterTestTask = true;
}
