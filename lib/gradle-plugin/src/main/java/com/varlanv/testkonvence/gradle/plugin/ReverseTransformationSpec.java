package com.varlanv.testkonvence.gradle.plugin;

import org.gradle.api.provider.Property;

abstract class ReverseTransformationSpec implements ReverseTransformationSpecView {

    public ReverseTransformationSpec() {}

    protected abstract Property<Boolean> getEnabled();

    @Override
    public void enabled(boolean toggle) {
        getEnabled().set(toggle);
    }
}
