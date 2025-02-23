package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.enforce.Train;
import com.varlanv.testkonvence.enforce.TrainOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class TestNameEnforceAction implements Action<Task> {

    private static final Logger log = Logging.getLogger(TestNameEnforceAction.class);
    ConfigurableFileCollection sourcesRootProp;
    ConfigurableFileCollection compileClasspath;
    ConfigurableFileCollection enforceFiles;
    Provider<Boolean> dryWithFailingProperty;

    @Override
    public void execute(Task task) {
        val sourcesRoots = sourcesRootProp.getFiles();
        if (sourcesRoots.isEmpty()) {
            log.debug("Source root is empty");
        } else if (sourcesRoots.size() > 1) {
            log.debug("More than single sources found");
        } else {
            val sourcesRoot = sourcesRoots.iterator().next().toPath();
            val sourceFiles = compileClasspath.getFiles();
            if (sourceFiles.isEmpty()) {
                log.debug("Source files are empty");
            } else if (Files.notExists(sourcesRoot)) {
                log.debug("Source root does not exist: {}", sourcesRoot);
            } else {
                for (val enforceFile : enforceFiles) {
                    if (Files.notExists(enforceFile.toPath())) {
                        throw new IllegalStateException("Enforce file does not exist: " + enforceFile);
                    }
                    new Train(
                            enforceFile.toPath(),
                            sourcesRoot,
                            sourceFiles.stream().map(File::toPath).collect(Collectors.toList()),
                            new TrainOptions(
                                    dryWithFailingProperty.get(),
                                    false
                            )
                    ).run();
                }
            }
        }
    }
}
