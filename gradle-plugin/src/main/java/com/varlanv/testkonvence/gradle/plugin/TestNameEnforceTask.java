package com.varlanv.testkonvence.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

public abstract class TestNameEnforceTask extends DefaultTask {

    public static String name(String testTaskName) {
        return testTaskName + "KonvenceEnforce";
    }

    public TestNameEnforceTask() {
        getDryWithFailing().convention(false);
        getUseCamelCaseMethodName().convention(false);
        getEnableReverseTransformation().convention(false);
    }

    @InputFiles
    public abstract ConfigurableFileCollection getSourcesRootProp();

    @InputFiles
    public abstract ConfigurableFileCollection getCompileClasspath();

    @InputFiles
    public abstract ConfigurableFileCollection getEnforceFiles();

    @Internal
    public abstract Property<Boolean> getDryWithFailing();

    @Internal
    public abstract Property<Boolean> getUseCamelCaseMethodName();

    @Internal
    public abstract Property<Boolean> getEnableReverseTransformation();

    @TaskAction
    public void execute() {
        new TestNameEnforceAction(
                        getSourcesRootProp(),
                        getCompileClasspath(),
                        getEnforceFiles(),
                        getDryWithFailing(),
                        getUseCamelCaseMethodName(),
                        getEnableReverseTransformation())
                .execute(this);
    }
}
