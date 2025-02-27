package com.varlanv.testkonvence.gradle.plugin;

import org.gradle.api.provider.Property;

abstract class ReverseTransformationSpec implements ReverseTransformationSpecView {

    protected abstract Property<Boolean> getEnabled();

    public ReverseTransformationSpec() {
        getEnabled().convention(true);
    }

    @Override
    public void enabled(boolean toggle) {
        getEnabled().set(toggle);
    }
}
