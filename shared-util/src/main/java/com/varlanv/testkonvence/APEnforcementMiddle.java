package com.varlanv.testkonvence;

import org.immutables.value.Value;

import java.util.SortedSet;

@Value.Immutable(builder = false)
public interface APEnforcementMiddle {

    @Value.Parameter
    String className();

    @Value.Parameter
    SortedSet<APEnforcementItem> methodEnforcements();
}
