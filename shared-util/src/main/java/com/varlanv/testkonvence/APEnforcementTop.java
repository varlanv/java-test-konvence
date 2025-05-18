package com.varlanv.testkonvence;

import org.immutables.value.Value;

@Value.Immutable
public interface APEnforcementTop {

    String fullEnclosingClassName();

    ImmutableList<APEnforcementMiddle> classEnforcements();
}
