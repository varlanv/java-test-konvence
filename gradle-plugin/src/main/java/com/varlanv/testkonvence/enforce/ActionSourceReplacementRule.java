package com.varlanv.testkonvence.enforce;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public final class ActionSourceReplacementRule implements SourceReplacementRule {

    @Getter
    SourceFile target;
    Function<List<String>, List<String>> action;

    @Override
    public List<String> apply(List<String> list) {
        return action.apply(list);
    }

    @Override
    public Boolean isNoop() {
        return false;
    }
}
