package com.varlanv.testkonvence.gradle.plugin;

import org.gradle.api.provider.Property;

abstract class TestKonvenceExtensionInternal implements TestKonvenceExtension {

    public abstract Property<Boolean> getApplyAutomaticallyAfterTestTask();

    public TestKonvenceExtensionInternal() {
        getApplyAutomaticallyAfterTestTask().convention(true);
    }

    @Override
    public void applyAutomaticallyAfterTestTask(boolean toggle) {
        getApplyAutomaticallyAfterTestTask().set(toggle);
    }
}
