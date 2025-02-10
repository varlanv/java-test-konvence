package com.varlanv.testkonvence;

import com.varlanv.testkonvence.info.XmlEnforceMeta;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
public class Train {

    Path resultXml;
    Path sourcesRoot;
    Collection<Path> sources;

    public void run() {
        var items = new XmlEnforceMeta().items(resultXml);
        var sourcesRootPath = sourcesRoot.toAbsolutePath().toString();
        var subject = new SourceReplacementTrain(
            new EnforcementMeta(
                items.stream().map(item -> {
                        var sourceFile = Paths.get(sourcesRootPath + File.separator + item.fullEnclosingClassName().replace(".", File.separator));
                        if (Files.exists(sourceFile) && Files.isRegularFile(sourceFile)) {
                            EnforceCandidate candidate;
                            var classNameParts = item.className().split("\\.");
                            var className = classNameParts[classNameParts.length - 1];
                            if (item.methodName().isEmpty()) {
                                candidate = new ClassNameFromDisplayName(item.displayName(), className);
                            } else {
                                candidate = new MethodNameFromDisplayName(item.displayName(), item.methodName());
                            }
                            return Optional.of(
                                new EnforcementMeta.Item(
                                    new EnforcedSourceFile(sourceFile.toAbsolutePath()),
                                    className,
                                    candidate
                                )
                            );
                        }
                        return Optional.<EnforcementMeta.Item>empty();
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList()
            )
        );
        subject.run();
    }
}
