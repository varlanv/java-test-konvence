package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.APEnforcementFull;
import com.varlanv.testkonvence.APEnforcementTop;
import com.varlanv.testkonvence.ImmutableAPEnforcementFull;
import com.varlanv.testkonvence.XmlMemoryEnforceMeta;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;

final class Train {

    private final Logger log;
    private final Path resultXml;
    private final Path sourcesRoot;
    private final TrainOptions trainOptions;

    Train(Logger log, Path resultXml, Path sourcesRoot, TrainOptions trainOptions) {
        this.log = log;
        this.resultXml = resultXml;
        this.sourcesRoot = sourcesRoot;
        this.trainOptions = trainOptions;
    }

    public void run() throws Exception {
        var items = XmlMemoryEnforceMeta.fromXmlPath(resultXml).entries();
        var sourcesRootPath = sourcesRoot.toAbsolutePath().toString();
        new SourceReplacementTrain(
                        log,
                        trainOptions,
                        new EnforcementMeta(
                                toItemsStream(items, sourcesRootPath).collect(Collectors.toList())))
                .run();
    }

    private Stream<EnforcementMeta.Item> toItemsStream(Collection<APEnforcementTop> topItems, String sourcesRootPath) {
        return topItems.stream()
                .flatMap(topItem -> topItem.classEnforcements().stream()
                        .flatMap(middleItem -> middleItem.methodEnforcements().stream()
                                .map(item -> ImmutableAPEnforcementFull.builder()
                                        .fullEnclosingClassName(topItem.fullEnclosingClassName())
                                        .className(middleItem.className())
                                        .displayName(item.displayName())
                                        .originalName(item.originalName())
                                        .newName(item.newName())
                                        .build())))
                .map(item -> parseItem(sourcesRootPath, item))
                .flatMap(Optional::stream);
    }

    private Optional<EnforcementMeta.Item> parseItem(String sourcesRootPath, APEnforcementFull item) {
        var sourceFile = Paths.get(sourcesRootPath + File.separator
                + item.fullEnclosingClassName().replace(".", File.separator) + ".java");
        if (Files.isRegularFile(sourceFile)) {
            var classNameParts = item.className().split("\\.");
            var className = classNameParts[classNameParts.length - 1];
            var absolutePath = sourceFile.toAbsolutePath();
            try {
                return Optional.of(new EnforcementMeta.Item(SourceFile.ofPath(absolutePath), className, item));
            } catch (Exception e) {
                log.debug("Failed to parse enforcement item [{}], skipping", absolutePath);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
