package com.varlanv.testkonvence.gradle.plugin;

import java.nio.file.Files;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;

@Getter
class TestNameEnforceAction implements Action<Task> {

    private static final Logger log = Logging.getLogger(TestNameEnforceAction.class);
    private final ConfigurableFileCollection sourcesRootProp;
    private final ConfigurableFileCollection compileClasspath;
    private final ConfigurableFileCollection enforceFiles;
    private final Provider<Boolean> dryWithFailingProvider;
    private final Provider<Boolean> camelCaseMethodNameProvider;
    private final Provider<Boolean> enableReverseTransformation;

    TestNameEnforceAction(
            ConfigurableFileCollection sourcesRootProp,
            ConfigurableFileCollection compileClasspath,
            ConfigurableFileCollection enforceFiles,
            Provider<Boolean> dryWithFailingProvider,
            Provider<Boolean> camelCaseMethodNameProvider,
            Provider<Boolean> enableReverseTransformation) {
        this.sourcesRootProp = sourcesRootProp;
        this.compileClasspath = compileClasspath;
        this.enforceFiles = enforceFiles;
        this.dryWithFailingProvider = dryWithFailingProvider;
        this.camelCaseMethodNameProvider = camelCaseMethodNameProvider;
        this.enableReverseTransformation = enableReverseTransformation;
    }

    public ConfigurableFileCollection sourcesRootProp() {
        return sourcesRootProp;
    }

    public ConfigurableFileCollection compileClasspath() {
        return compileClasspath;
    }

    public ConfigurableFileCollection enforceFiles() {
        return enforceFiles;
    }

    public Provider<Boolean> dryWithFailingProvider() {
        return dryWithFailingProvider;
    }

    public Provider<Boolean> camelCaseMethodNameProvider() {
        return camelCaseMethodNameProvider;
    }

    public Provider<Boolean> enableReverseTransformation() {
        return enableReverseTransformation;
    }

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
                                    new TrainOptions(
                                            dryWithFailingProvider.get(),
                                            enableReverseTransformation.get(),
                                            camelCaseMethodNameProvider.get()))
                            .run();
                }
            }
        }
    }
}
