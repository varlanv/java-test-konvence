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

    @InputFiles
    public abstract ConfigurableFileCollection getSourcesRootProp();

    @InputFiles
    public abstract ConfigurableFileCollection getCompileClasspath();

    @InputFiles
    public abstract ConfigurableFileCollection getEnforceFiles();

    @Internal
    public abstract Property<Boolean> getDryWithFailing();

    @TaskAction
    public void execute() {
        new TestNameEnforceAction(
            getSourcesRootProp(),
            getCompileClasspath(),
            getEnforceFiles(),
            getDryWithFailing()
        ).execute(this);
    }
}
