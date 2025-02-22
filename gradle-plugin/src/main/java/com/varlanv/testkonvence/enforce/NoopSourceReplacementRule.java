package com.varlanv.testkonvence.enforce;

import lombok.Value;

import java.util.List;

@Value
public class NoopSourceReplacementRule implements SourceReplacementRule {

    SourceFile target;

    @Override
    public List<String> apply(List<String> list) {
        return list;
    }

    @Override
    public Boolean isNoop() {
        return true;
    }
}
