package com.varlanv.testkonvence;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(strictBuilder = true)
public interface APEnforcementFull {

    String fullEnclosingClassName();

    String className();

    String displayName();

    String originalName();

    String newName();
}
