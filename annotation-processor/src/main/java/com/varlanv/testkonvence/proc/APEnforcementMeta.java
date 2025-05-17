package com.varlanv.testkonvence.proc;

import com.varlanv.testkonvence.ImmutableList;
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
