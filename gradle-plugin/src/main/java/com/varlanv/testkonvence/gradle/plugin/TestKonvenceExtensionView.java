package com.varlanv.testkonvence.gradle.plugin;

import org.gradle.api.Action;

/** Configuration options for test-konvence plugin. */
public interface TestKonvenceExtensionView {

    /** @return name of the extension. */
    static String name() {
        return "testKonvence";
    }

    /**
     * Whether to apply plugin logic. Setting this to {@code false} will completely disable tasks
     * registration/processing.
     *
     * <p>Default is {@code true}
     *
     * @param toggle state
     */
    void enabled(boolean toggle);

    /**
     * Whether to run enforce task after executing any test task.
     *
     * <p>Default is {@code true}
     *
     * @param toggle - state
     */
    void applyAutomaticallyAfterTestTask(boolean toggle);

    /**
     * Configures reverse transformation, that is, generating {@code @DisplayName} annotation from test name for tests
     * without {@code @DisplayName} annotation.
     *
     * @param action configuration
     */
    void reverseTransformation(Action<ReverseTransformationSpecView> action);

    /**
     * Whether to use camel case instead of snake case for generating test name from display name
     *
     * <p>Default is {@code false}
     *
     * @param toggle - state
     */
    void useCamelCaseForMethodNames(boolean toggle);
}
