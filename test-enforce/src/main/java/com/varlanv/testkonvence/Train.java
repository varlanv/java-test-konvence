package com.varlanv.testkonvence;

import com.varlanv.testkonvence.info.XmlEnforceMeta;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Train {

    Path resultXml;
    Path sourcesRoot;
    Collection<Path> sources;

    public void run() {
        val items = new XmlEnforceMeta().items(resultXml);
        val sourcesRootPath = sourcesRoot.toAbsolutePath().toString();
        val subject = new SourceReplacementTrain(
            new EnforcementMeta(
                items.stream().map(item -> {
                        val sourceFile = Paths.get(sourcesRootPath + File.separator + item.fullEnclosingClassName().replace(".", File.separator));
                        if (Files.exists(sourceFile) && Files.isRegularFile(sourceFile)) {
                            EnforceCandidate candidate;
                            val classNameParts = item.className().split("\\.");
                            val className = classNameParts[classNameParts.length - 1];
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
                    .collect(Collectors.toList()))
        );
        subject.run();
    }
}
