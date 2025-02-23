package com.varlanv.testkonvence.enforce;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public final class TrainOptions {

    @Builder.Default
    Boolean dryWithFailing = false;
    @Builder.Default
    Boolean reverseTransformation = false;
    @Builder.Default
    Boolean camelCaseMethodName = false;
}
