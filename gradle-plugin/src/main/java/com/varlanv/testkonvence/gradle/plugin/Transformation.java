package com.varlanv.testkonvence.gradle.plugin;

import java.util.function.Function;

interface Transformation {

    TransformationTuple input();

    Function<SourceLines, SourceLines> action();

    static Transformation of(
            SourceLines sourceLines, EnforcementMeta.Item meta, Function<SourceLines, SourceLines> action) {

        return new Transformation() {

            @Override
            public TransformationTuple input() {
                return TransformationTuple.of(sourceLines, meta);
            }

            @Override
            public Function<SourceLines, SourceLines> action() {
                return action;
            }
        };
    }
}
