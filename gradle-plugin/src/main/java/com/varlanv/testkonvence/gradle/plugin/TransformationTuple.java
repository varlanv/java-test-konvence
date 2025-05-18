package com.varlanv.testkonvence.gradle.plugin;

interface TransformationTuple {

    SourceLines sourceLines();

    EnforcementMeta.Item meta();

    static TransformationTuple of(SourceLines sourceLines, EnforcementMeta.Item meta) {
        return new TransformationTuple() {

            @Override
            public SourceLines sourceLines() {
                return sourceLines;
            }

            @Override
            public EnforcementMeta.Item meta() {
                return meta;
            }
        };
    }
}
