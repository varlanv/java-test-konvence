package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testnameconvention.*;
import com.varlanv.testnameconvention.info.XmlEnforceMeta;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

public abstract class TestNameEnforceTask extends DefaultTask {

    public static String name() {
        return "testKonvenceEnforce";
    }

    @InputFile
    public abstract RegularFileProperty getSourcesRoot();

    @InputFiles
    public abstract ConfigurableFileCollection getCompileClasspath();

    @InputFile
    public abstract ConfigurableFileCollection getEnforceFiles();

    @TaskAction
    public void enforce() {
        var enforceFiles = getEnforceFiles().getFiles();
        var sourcesRoot = getSourcesRoot().getAsFile().get().getAbsolutePath();
        var sourceFileTree = getCompileClasspath().getAsFileTree();
        enforceFiles.forEach(enforceFile -> {
            var items = new XmlEnforceMeta().items(enforceFile.toPath());
            var subject = new SourceReplacementTrain(
                new EnforcementMeta(
                    items.stream().map(item -> {
                                var sourceFilePath = item.fullEnclosingClassName().replace(".", "/");
                                var sourceFile = sourceFileTree.filter(f -> f.getAbsolutePath().equals(sourcesRoot + sourceFilePath)).getSingleFile();
                                EnforceCandidate candidate;
                                var classNameParts = item.className().split("\\.");
                                var className = classNameParts[classNameParts.length - 1];
                                if (item.methodName().isEmpty()) {
                                    candidate = new ClassNameFromDisplayName(item.displayName(), className);
                                } else {
                                    candidate = new MethodNameFromDisplayName(item.displayName(), item.methodName());
                                }
                                return new EnforcementMeta.Item(
                                    new EnforcedSourceFile(sourceFile.getAbsolutePath()),
                                    className,
                                    candidate
                                );
                            }
                        )
                        .toList()
                )
            );
            subject.run();
        });
    }
}
