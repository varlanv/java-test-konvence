package com.varlanv.testkonvence;

import org.immutables.value.Value;

@Value.Immutable
public interface APEnforcementItem {

    String displayName();

    String originalName();

    String newName();
}
