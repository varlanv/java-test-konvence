package com.varlanv.testkonvence;

import java.util.SortedSet;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
@Value.Style(strictBuilder = true)
public interface APEnforcementMiddle extends Comparable<APEnforcementMiddle> {

    @Value.Parameter
    String className();

    @Value.Parameter
    SortedSet<APEnforcementItem> methodEnforcements();

    @Override
    default int compareTo(@NotNull APEnforcementMiddle o) {
        return className().compareTo(o.className());
    }
}
