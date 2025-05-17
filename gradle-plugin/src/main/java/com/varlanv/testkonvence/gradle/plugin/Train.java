package com.varlanv.testkonvence.gradle.plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

class Train {

    private final Path resultXml;
    private final Path sourcesRoot;
    private final TrainOptions trainOptions;

    Train(Path resultXml, Path sourcesRoot, TrainOptions trainOptions) {
        this.resultXml = resultXml;
        this.sourcesRoot = sourcesRoot;
        this.trainOptions = trainOptions;
    }

    public void run() throws Exception {
        var items = new XmlEnforceMeta().items(resultXml);
        var sourcesRootPath = sourcesRoot.toAbsolutePath().toString();
        new SourceReplacementTrain(
                        trainOptions,
                        new EnforcementMeta(items.stream()
                                .map(item -> {
                                    var sourceFile = Paths.get(sourcesRootPath + File.separator
                                            + item.fullEnclosingClassName().replace(".", File.separator) + ".java");
                                    if (Files.isRegularFile(sourceFile)) {
                                        var classNameParts = item.className().split("\\.");
                                        var className = classNameParts[classNameParts.length - 1];
                                        try {
                                            return Optional.of(new EnforcementMeta.Item(
                                                    SourceFile.ofPath(sourceFile.toAbsolutePath()),
                                                    className,
                                                    resolveEnforceCandidate(item, className)));
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    return Optional.<EnforcementMeta.Item>empty();
                                })
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList())))
                .run();
    }

    private EnforceCandidate resolveEnforceCandidate(APEnforcementMeta.Item item, String className) {
        if (item.methodName().isEmpty()) {
            return new ClassNameFromDisplayName(item.displayName(), className);
        } else {
            var snake = new SnakeMethodNameFromDisplayName(item.displayName(), item.methodName());
            if (trainOptions.camelCaseMethodName()) {
                return new CamelMethodNameFromDisplayName(snake);
            } else {
                return snake;
            }
        }
    }
}
