package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Constants;
import com.varlanv.testkonvence.TrustedException;
import java.nio.file.Files;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TestNameEnforceAction implements Action<Task> {

    private static final Logger log = LoggerFactory.getLogger(TestNameEnforceAction.class);
    private final ConfigurableFileCollection sourcesRootProp;
    private final ConfigurableFileCollection compileClasspath;
    private final ConfigurableFileCollection enforceFiles;
    private final Provider<Boolean> pluginEnabled;
    private final Provider<Boolean> dryWithFailingProvider;
    private final Provider<Boolean> camelCaseMethodNameProvider;
    private final Provider<Boolean> enableReverseTransformation;
    private final Provider<Boolean> performanceLogEnabled;

    TestNameEnforceAction(
            ConfigurableFileCollection sourcesRootProp,
            ConfigurableFileCollection compileClasspath,
            ConfigurableFileCollection enforceFiles,
            Provider<Boolean> pluginEnabled,
            Provider<Boolean> dryWithFailingProvider,
            Provider<Boolean> camelCaseMethodNameProvider,
            Provider<Boolean> enableReverseTransformation,
            Provider<Boolean> performanceLogEnabled) {
        this.sourcesRootProp = sourcesRootProp;
        this.compileClasspath = compileClasspath;
        this.enforceFiles = enforceFiles;
        this.pluginEnabled = pluginEnabled;
        this.dryWithFailingProvider = dryWithFailingProvider;
        this.camelCaseMethodNameProvider = camelCaseMethodNameProvider;
        this.enableReverseTransformation = enableReverseTransformation;
        this.performanceLogEnabled = performanceLogEnabled;
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

    public Provider<Boolean> performanceLogEnabled() {
        return performanceLogEnabled;
    }

    @Override
    public void execute(Task task) {
        if (pluginEnabled.get()) {
            try {
                apply();
            } catch (TrustedException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (Exception e) {
                var message = e.getMessage();
                log.error(
                        "Failed to apply Gradle plugin [{}], "
                                + "Build will not be failed, but plugin logic might not have been applied. "
                                + "Internal error message is: {}",
                        Constants.PLUGIN_NAME,
                        message == null || message.isBlank() ? "<no message>" : message);
            }
        } else {
            log.debug("Gradle plugin [{}] is disabled, ignoring task action.", Constants.PLUGIN_NAME);
        }
    }

    private void apply() throws Exception {
        boolean isPerformanceLogEnabled = performanceLogEnabled.get();
        var performanceLog = new TrainPerformanceLog(isPerformanceLogEnabled, log);
        try {
            performanceLog.acceptTotal(() -> {
                var sourcesRoots = sourcesRootProp.getFiles();
                if (sourcesRoots.isEmpty()) {
                    log.debug("Source root is empty");
                } else if (sourcesRoots.size() > 1) {
                    log.debug("More than single sources found");
                } else {
                    var sourcesRoot = sourcesRoots.iterator().next().toPath();
                    var sourceFiles = compileClasspath.getFiles();
                    if (sourceFiles.isEmpty()) {
                        log.debug("Source files are empty");
                    } else if (Files.notExists(sourcesRoot)) {
                        log.debug("Source root does not exist: {}", sourcesRoot);
                    } else {
                        for (var enforceFile : enforceFiles) {
                            if (Files.notExists(enforceFile.toPath())) {
                                throw new IllegalStateException("Enforce file does not exist: " + enforceFile);
                            }
                            new Train(
                                            log,
                                            enforceFile.toPath(),
                                            sourcesRoot,
                                            ImmutableTrainOptions.builder()
                                                    .dryWithFailing(dryWithFailingProvider.get())
                                                    .reverseTransformation(enableReverseTransformation.get())
                                                    .performanceLogEnabled(isPerformanceLogEnabled)
                                                    .build())
                                    .run();
                        }
                    }
                }
            });
        } finally {
            performanceLog.printResult();
        }
    }
}
