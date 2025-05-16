package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Constants;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
class ConfigureOnBeforeCompileTestStart implements Action<Task> {

    private static final Logger log = Logging.getLogger(ConfigureOnBeforeCompileTestStart.class);
    Provider<Path> annotationProcessorTargetPathProvider;

    @Override
    @SneakyThrows
    public void execute(Task task) {
        val targetPath = annotationProcessorTargetPathProvider.get();
        if (Files.notExists(targetPath)) {
            val parentPath = targetPath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
                try (val in = ConfigureOnBeforeCompileTestStart.class.getResourceAsStream(Constants.PROCESSOR_JAR_RESOURCE)) {
                    if (in == null) {
                        log.error("Unable to find processor jar file [{}]", Constants.PROCESSOR_JAR);
                    } else {
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }
}
