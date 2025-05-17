package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Constants;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;

final class ConfigureOnBeforeCompileTestStart implements Action<Task> {

    private static final Logger log = Logging.getLogger(ConfigureOnBeforeCompileTestStart.class);
    private final Provider<Path> annotationProcessorTargetPathProvider;

    ConfigureOnBeforeCompileTestStart(Provider<Path> annotationProcessorTargetPathProvider) {
        this.annotationProcessorTargetPathProvider = annotationProcessorTargetPathProvider;
    }

    @Override
    public void execute(Task task) {
        try {
            var targetPath = annotationProcessorTargetPathProvider.get();
            if (Files.notExists(targetPath)) {
                var parentPath = targetPath.getParent();
                if (parentPath != null) {
                    Files.createDirectories(parentPath);
                    try (var in = ConfigureOnBeforeCompileTestStart.class.getResourceAsStream(
                            Constants.PROCESSOR_JAR_RESOURCE)) {
                        if (in == null) {
                            log.debug("Unable to find processor jar file [{}]", Constants.PROCESSOR_JAR);
                        } else {
                            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Unable to configure annotation processor for test compilation, will not apply plugin", e);
        }
    }
}
