package com.varlanv.testkonvence.gradle.plugin;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;

abstract class TestKonvenceExtension implements TestKonvenceExtensionView {

    protected abstract Property<Boolean> getEnabled();

    protected abstract Property<Boolean> getApplyAutomaticallyAfterTestTask();

    protected abstract Property<Boolean> getCamelCaseMethodNameProperty();

    protected abstract Property<ReverseTransformationSpec> getReverseTransformation();

    public TestKonvenceExtension() {}

    @Override
    public void enabled(boolean toggle) {
        getEnabled().set(toggle);
    }

    @Override
    public void applyAutomaticallyAfterTestTask(boolean toggle) {
        getApplyAutomaticallyAfterTestTask().set(toggle);
    }

    @Override
    public void reverseTransformation(Action<ReverseTransformationSpecView> action) {
        action.execute(getReverseTransformation().get());
    }

    @Override
    public void useCamelCaseForMethodNames(boolean toggle) {
        getCamelCaseMethodNameProperty().set(toggle);
    }
}
