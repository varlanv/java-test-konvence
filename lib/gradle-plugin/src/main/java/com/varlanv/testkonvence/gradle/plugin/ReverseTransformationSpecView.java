package com.varlanv.testkonvence.gradle.plugin;

/**
 * Configuration spec for reverse transformation, that is, generating {@code @DisplayName} annotation from test name for
 * tests without {@code @DisplayName} annotation.
 */
public interface ReverseTransformationSpecView {

    /**
     * Whether to apply reverse transformation for {@code @DisplayName} annotation.
     *
     * <p>Default is {@code false}
     *
     * @param toggle - state
     */
    void enabled(boolean toggle);
}
