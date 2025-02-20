package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Train;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.stream.Collectors;

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
        val sourcesRoot = getSourcesRoot().getAsFile().get().toPath();
        val sourceFiles = getCompileClasspath().getFiles();

        for (val enforceFile : getEnforceFiles()) {
            new Train(
                enforceFile.toPath(),
                sourcesRoot,
                sourceFiles.stream().map(File::toPath).collect(Collectors.toList())
            ).run();
        }
    }
}
