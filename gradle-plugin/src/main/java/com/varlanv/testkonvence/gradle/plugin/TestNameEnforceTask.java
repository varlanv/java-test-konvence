package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.enforce.Train;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

public abstract class TestNameEnforceTask extends DefaultTask {

    private static final Logger log = Logging.getLogger(TestNameEnforceTask.class);

    public static String name(String testTaskName) {
        return testTaskName + "KonvenceEnforce";
    }

    @InputFiles
    public abstract ConfigurableFileCollection getSourcesRootProp();

    @InputFiles
    public abstract ConfigurableFileCollection getCompileClasspath();

    @InputFiles
    public abstract ConfigurableFileCollection getEnforceFiles();

//    @OutputFiles
//    public abstract ConfigurableFileCollection getEnforceFilesCollection();


    @TaskAction
    public void execute() {
        val sourcesRoots = getSourcesRootProp().getFiles();
        if (sourcesRoots.isEmpty()) {
            log.debug("Source root is empty");
        } else if (sourcesRoots.size() > 1) {
            log.debug("More than single sources found");
        } else {
            val sourcesRoot = sourcesRoots.iterator().next().toPath();
            val sourceFiles = getCompileClasspath().getFiles();
            if (sourceFiles.isEmpty()) {
                log.debug("Source files are empty");
            } else if (Files.notExists(sourcesRoot)) {
                log.debug("Source root does not exist: {}", sourcesRoot);
            } else {
                for (val enforceFile : getEnforceFiles()) {
                    if (Files.notExists(enforceFile.toPath())) {
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
    }
}
