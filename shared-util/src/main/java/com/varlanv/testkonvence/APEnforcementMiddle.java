package com.varlanv.testkonvence;

import org.immutables.value.Value;

@Value.Immutable(builder = false)
public interface APEnforcementMiddle {

    @Value.Parameter
    String className();

    @Value.Parameter
    ImmutableList<APEnforcementItem> items();
}
