package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.APEnforcementMetaItem;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Train {

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
                        new EnforcementMeta(
                                toItemsStream(items, sourcesRootPath).collect(Collectors.toList())))
                .run();
    }

    private Stream<EnforcementMeta.Item> toItemsStream(List<APEnforcementMetaItem> items, String sourcesRootPath) {
        return items.stream().map(item -> parseItem(sourcesRootPath, item)).flatMap(Optional::stream);
    }

    private Optional<EnforcementMeta.Item> parseItem(String sourcesRootPath, APEnforcementMetaItem item) {
        var sourceFile = Paths.get(sourcesRootPath + File.separator
                + item.fullEnclosingClassName().replace(".", File.separator) + ".java");
        if (Files.isRegularFile(sourceFile)) {
            var classNameParts = item.className().split("\\.");
            var className = classNameParts[classNameParts.length - 1];
            try {
                return Optional.<EnforcementMeta.Item>of(new EnforcementMeta.Item(
                        SourceFile.ofPath(sourceFile.toAbsolutePath()),
                        className,
                        resolveEnforceCandidate(item, className)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.<EnforcementMeta.Item>empty();
    }

    private EnforceCandidate resolveEnforceCandidate(APEnforcementMetaItem item, String className) {
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
