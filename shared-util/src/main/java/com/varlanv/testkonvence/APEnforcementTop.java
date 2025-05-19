package com.varlanv.testkonvence;

import java.util.SortedSet;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

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
