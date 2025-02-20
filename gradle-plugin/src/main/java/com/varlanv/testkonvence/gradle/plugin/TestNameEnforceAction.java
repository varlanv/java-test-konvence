package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Train;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TestNameEnforceAction implements Action<Task> {

    public static String name() {
        return "testKonvenceEnforce";
    }

    RegularFileProperty sourcesRootProp;

    ConfigurableFileCollection compileClasspath;

    ConfigurableFileCollection enforceFiles;

    @Override
    public void execute(Task task) {
        val sourcesRoot = sourcesRootProp.getAsFile().get().toPath();
        val sourceFiles = compileClasspath.getFiles();
        if (sourceFiles.isEmpty()) {
            throw new IllegalStateException("No sources found");
        } else if (Files.notExists(sourcesRoot)) {
            throw new IllegalStateException("Source root does not exist: " + sourcesRoot);
        }
        for (val enforceFile : enforceFiles) {
            if (Files.notExists(enforceFile.toPath())){
                throw new IllegalStateException("Enforce file does not exist: " + enforceFile);
            }
            new Train(
                enforceFile.toPath(),
                sourcesRoot,
                sourceFiles.stream().map(File::toPath).collect(Collectors.toList())
            ).run();
        }
    }
}
