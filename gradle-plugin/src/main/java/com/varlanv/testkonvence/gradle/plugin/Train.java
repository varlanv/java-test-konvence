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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

final class Train {

    private final Logger log;
    private final Path resultXml;
    private final Path sourcesRoot;
    private final TrainOptions trainOptions;
    private final TrainPerformanceLog performanceLog;

    Train(Logger log, Path resultXml, Path sourcesRoot, TrainOptions trainOptions, TrainPerformanceLog performanceLog) {
        this.log = log;
        this.resultXml = resultXml;
        this.sourcesRoot = sourcesRoot;
        this.trainOptions = trainOptions;
        this.performanceLog = performanceLog;
    }

    Train(Logger log, Path resultXml, Path sourcesRoot, TrainOptions trainOptions) {
        this(
                log,
                resultXml,
                sourcesRoot,
                trainOptions,
                new TrainPerformanceLog(trainOptions.performanceLogEnabled(), log));
    }

    public void run() throws Exception {
        var items = performanceLog.printIntermediateSupplier(
                () -> "Parse enforce xml",
                () -> XmlMemoryEnforceMeta.fromXmlPath(resultXml).entries());
        var sourcesRootPath = sourcesRoot.toAbsolutePath().toString();
        new SourceReplacementTrain(
                        log,
                        trainOptions,
                        new EnforcementMeta(
                                toItemsStream(items, sourcesRootPath).collect(Collectors.toList())),
                        performanceLog)
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
                .filter(Objects::nonNull);
    }

    private EnforcementMeta.@Nullable Item parseItem(String sourcesRootPath, APEnforcementFull item) {
        var sourceFile = Paths.get(sourcesRootPath + File.separator
                + item.fullEnclosingClassName().replace(".", File.separator) + ".java");
        if (Files.isRegularFile(sourceFile)) {
            var classNameParts = item.className().split("\\.");
            var className = classNameParts[classNameParts.length - 1];
            var absolutePath = sourceFile.toAbsolutePath();
            var topClassNameParts = item.fullEnclosingClassName().split("\\.");
            var topClassName = topClassNameParts[topClassNameParts.length - 1];
            try {
                return new EnforcementMeta.Item(SourceFile.ofPath(absolutePath), className, topClassName, item);
            } catch (Exception e) {
                log.debug("Failed to parse enforcement item [{}], skipping", absolutePath);
                return null;
            }
        }
        return null;
    }
}
