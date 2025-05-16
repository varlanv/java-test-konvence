package com.varlanv.testkonvence.gradle.plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
class Train {

    Path resultXml;
    Path sourcesRoot;
    TrainOptions trainOptions;

    public void run() {
        val items = new XmlEnforceMeta().items(resultXml);
        val sourcesRootPath = sourcesRoot.toAbsolutePath().toString();
        new SourceReplacementTrain(
                        trainOptions,
                        new EnforcementMeta(items.stream()
                                .map(item -> {
                                    val sourceFile = Paths.get(sourcesRootPath + File.separator
                                            + item.fullEnclosingClassName().replace(".", File.separator) + ".java");
                                    if (Files.isRegularFile(sourceFile)) {
                                        val classNameParts = item.className().split("\\.");
                                        val className = classNameParts[classNameParts.length - 1];
                                        return Optional.of(new EnforcementMeta.Item(
                                                SourceFile.ofPath(sourceFile.toAbsolutePath()),
                                                className,
                                                resolveEnforceCandidate(item, className)));
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
            val snake = new SnakeMethodNameFromDisplayName(item.displayName(), item.methodName());
            if (trainOptions.camelCaseMethodName()) {
                return new CamelMethodNameFromDisplayName(snake);
            } else {
                return snake;
            }
        }
    }
}
