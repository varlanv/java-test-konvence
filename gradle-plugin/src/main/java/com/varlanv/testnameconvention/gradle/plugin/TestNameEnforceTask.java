package com.varlanv.testnameconvention.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class TestNameEnforceTask extends DefaultTask {

    public static String name() {
        return "enforceTestNaming";
    }

    @InputFile
    public abstract RegularFileProperty getInputFile();

    @TaskAction
    public void enforce() {
    }
}
