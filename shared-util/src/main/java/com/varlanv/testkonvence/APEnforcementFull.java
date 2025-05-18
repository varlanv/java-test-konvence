package com.varlanv.testkonvence;

import org.immutables.value.Value;

@Value.Immutable
public interface APEnforcementFull {

    String fullEnclosingClassName();

    String className();

    String displayName();

    String methodName();

    String newName();

    boolean reversed();
}
