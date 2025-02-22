package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.enforce.Train;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TestNameEnforceAction implements Action<Task> {

    private static final Logger log = Logging.getLogger(TestNameEnforceAction.class);

    public static String name() {
        return "testKonvenceEnforce";
    }

    ConfigurableFileCollection sourcesRootProp;

    ConfigurableFileCollection compileClasspath;

    ConfigurableFileCollection enforceFiles;

    @Override
    public void execute(Task task) {
        val sourcesRoots = sourcesRootProp.getFiles();
        if (sourcesRoots.isEmpty()) {
            log.error("No sources found");
        } else if (sourcesRoots.size() > 1) {
            log.error("More than single sources found");
        } else {
            val sourcesRoot = sourcesRoots.iterator().next().toPath();
            val sourceFiles = compileClasspath.getFiles();
            if (sourceFiles.isEmpty()) {
                throw new IllegalStateException("No sources found");
            } else if (Files.notExists(sourcesRoot)) {
                throw new IllegalStateException("Source root does not exist: " + sourcesRoot);
            }
            for (val enforceFile : enforceFiles) {
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
