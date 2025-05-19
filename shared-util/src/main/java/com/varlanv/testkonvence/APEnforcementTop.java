package com.varlanv.testkonvence;

import org.immutables.value.Value;

import java.util.SortedSet;

@Value.Immutable(builder = false)
public interface APEnforcementTop {

    @Value.Parameter
    String fullEnclosingClassName();

    @Value.Parameter
    SortedSet<APEnforcementMiddle> classEnforcements();
}
