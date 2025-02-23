package com.varlanv.testkonvence.gradle.plugin;

import org.gradle.api.provider.Property;

abstract class TestKonvenceExtensionInternal implements TestKonvenceExtension {

    public abstract Property<Boolean> getApplyAutomaticallyAfterTestTask();

    public abstract Property<Boolean> getCamelCaseMethodNameProperty();

    public abstract Property<Boolean> getEnableReverseTransformation();

    public TestKonvenceExtensionInternal() {
        getApplyAutomaticallyAfterTestTask().convention(true);
        getEnableReverseTransformation().convention(false);
        getCamelCaseMethodNameProperty().convention(false);
    }

    @Override
    public void applyAutomaticallyAfterTestTask(boolean toggle) {
        getApplyAutomaticallyAfterTestTask().set(toggle);
    }

    @Override
    public void enableReverseTransformation(boolean toggle) {
        getEnableReverseTransformation().set(toggle);
    }

    @Override
    public void useCamelCaseForMethodNames(boolean toggle) {
        getCamelCaseMethodNameProperty().set(toggle);
    }
}
