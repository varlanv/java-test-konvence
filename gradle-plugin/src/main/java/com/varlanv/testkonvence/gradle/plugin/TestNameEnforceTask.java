package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Train;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

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
        var sourcesRoot = getSourcesRoot().getAsFile().get().toPath();
        var sourceFiles = getCompileClasspath().getFiles();

        for (var enforceFile : getEnforceFiles()) {
            new Train(
                enforceFile.toPath(),
                sourcesRoot,
                sourceFiles.stream().map(File::toPath).toList()
            ).run();
        }
    }
}
