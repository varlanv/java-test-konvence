package com.varlanv.testkonvence.proc;

import org.immutables.value.Value;

@Value.Immutable
interface APEnforcementMeta {

    ImmutableList<Item> items();

    @Value.Immutable
    interface Item {

        String fullEnclosingClassName();

        String displayName();

        String className();

        String methodName();
    }
}
