package com.varlanv.testkonvence.gradle.plugin;

import lombok.val;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

interface Transformations {

    Transformations register(Transformation transformation);

    void consumeGroupedByFile(BiConsumer<SourceFile, List<Transformation>> action);

    static Transformations empty() {
        val transformationsList = new ArrayList<Transformation>(0);
        return new Transformations() {

            @Override
            public Transformations register(Transformation transformation) {
                transformationsList.add(transformation);
                return this;
            }

            @Override
            public void consumeGroupedByFile(BiConsumer<SourceFile, List<Transformation>> action) {
                if (transformationsList.isEmpty()) {
                    return;
                }
                val grouped = new LinkedHashMap<String, Pair<SourceFile, List<Transformation>>>();
                transformationsList.forEach(transformation -> {
                    val sourceFile = transformation.input().meta().sourceFile();
                    grouped.computeIfAbsent(
                        sourceFile.path().toString(),
                        k -> Pair.of(sourceFile, new ArrayList<>(1))
                    ).right().add(transformation);
                });
                grouped.forEach((k, v) -> action.accept(v.left(), v.right()));
            }
        };
    }

    interface Transformation {

        TransformationTuple input();

        Function<SourceLines, SourceLines> action();

        static Transformation of(SourceLines sourceLines, EnforcementMeta.Item meta, Function<SourceLines, SourceLines> action) {

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
}
