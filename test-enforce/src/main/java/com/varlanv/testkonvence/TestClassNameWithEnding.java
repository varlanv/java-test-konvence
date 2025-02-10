package com.varlanv.testkonvence;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TestClassNameWithEnding implements EnforceCandidate {

    EnforceCandidate delegate;

    @Override
    public String displayName() {
        return delegate.displayName();
    }

    @Override
    public String originalName() {
        return delegate.originalName();
    }

    @Override
    public String newName() {
        var newName = delegate.newName();
        if (newName.length() < 4) {
            return newName + "Test";
        } else {
            if (!newName.endsWith("Test") || !newName.endsWith("Spec")) {
                return newName + "Test";
            } else {
                return newName;
            }
        }
    }

    @Override
    public Kind kind() {
        return delegate.kind();
    }
}
