package com.varlanv.testkonvence;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;

@Value.Immutable
@Value.Style(strictBuilder = true)
public interface APEnforcementTop extends Comparable<APEnforcementTop> {

    @Value.Parameter
    String fullEnclosingClassName();

    @Value.Parameter
    SortedSet<APEnforcementMiddle> classEnforcements();

    @Override
    default int compareTo(@NotNull APEnforcementTop o) {
        return fullEnclosingClassName().compareTo(o.fullEnclosingClassName());
    }
}
