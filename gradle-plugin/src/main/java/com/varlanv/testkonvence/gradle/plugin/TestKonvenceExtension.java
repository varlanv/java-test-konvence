package com.varlanv.testkonvence.gradle.plugin;

public interface TestKonvenceExtension {

    String EXTENSION_NAME = "testKonvence";

    void applyAutomaticallyAfterTestTask(boolean toggle);

    void enableReverseTransformation();
}
