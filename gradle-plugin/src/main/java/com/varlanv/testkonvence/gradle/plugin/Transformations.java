package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Pair;
import com.varlanv.testkonvence.ThrowingBiConsumer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

interface Transformations {

    Transformations register(Transformation transformation);

    void consumeGroupedByFile(ThrowingBiConsumer<SourceFile, List<Transformation>> action) throws Exception;

    static Transformations empty() {
        var transformationsList = new ArrayList<Transformation>(0);
        return new Transformations() {

            @Override
            public Transformations register(Transformation transformation) {
                transformationsList.add(transformation);
                return this;
            }

            @Override
            public void consumeGroupedByFile(ThrowingBiConsumer<SourceFile, List<Transformation>> action)
                    throws Exception {
                if (transformationsList.isEmpty()) {
                    return;
                }
                var grouped = new LinkedHashMap<String, Pair<SourceFile, List<Transformation>>>();
                transformationsList.forEach(transformation -> {
                    var sourceFile = transformation.input().meta().sourceFile();
                    grouped.computeIfAbsent(sourceFile.path().toString(), k -> Pair.of(sourceFile, new ArrayList<>(1)))
                            .right()
                            .add(transformation);
                });
                for (var v : grouped.values()) {
                    action.accept(v.left(), v.right());
                }
            }
        };
    }
}
