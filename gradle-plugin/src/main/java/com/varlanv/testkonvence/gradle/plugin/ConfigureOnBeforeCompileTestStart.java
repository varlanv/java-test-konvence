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
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
class ConfigureOnBeforeCompileTestStart implements Action<Task> {

    private static final Logger log = Logging.getLogger(ConfigureOnBeforeCompileTestStart.class);
    Provider<Path> pluginDirPathProvider;

    @Override
    public void execute(Task task) {
        if (!(task instanceof JavaCompile)) {
            throw new IllegalArgumentException(
                String.format(
                    "Task must be of type '[%s], but received: [%s]'. This is likely caused by a bug in the plugin [%s].",
                    Test.class.getName(), task.getClass().getName(), Constants.PLUGIN_NAME
                )
            );
        }
        setupAnnotationProcessorJar();
    }

    @SneakyThrows
    public void setupAnnotationProcessorJar() {
        val pluginDir = Files.createDirectories(pluginDirPathProvider.get());
        val targetJarPath = pluginDir.resolve(Constants.PROCESSOR_JAR);
        if (Files.notExists(targetJarPath)) {
            try (val in = ConfigureOnBeforeCompileTestStart.class.getResourceAsStream(Constants.PROCESSOR_JAR_RESOURCE)) {
                if (in == null) {
                    log.error("Unable to find processor jar file [{}]", Constants.PROCESSOR_JAR);
                } else {
                    Files.copy(in, targetJarPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
