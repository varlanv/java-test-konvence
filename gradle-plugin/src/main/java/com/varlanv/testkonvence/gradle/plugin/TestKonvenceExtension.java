package com.varlanv.testkonvence.gradle.plugin;

/**
 * Configuration options for test-konvence plugin.
 */
public interface TestKonvenceExtension {

    static String name() {
        return "testKonvence";
    }

    void applyAutomaticallyAfterTestTask(boolean toggle);

    void enableReverseTransformation(boolean toggle);

    void useCamelCaseForMethodNames(boolean toggle);
}
