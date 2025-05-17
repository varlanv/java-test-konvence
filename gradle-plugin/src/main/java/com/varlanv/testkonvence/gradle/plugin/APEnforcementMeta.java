package com.varlanv.testkonvence.gradle.plugin;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable(builder = false)
interface APEnforcementMeta {

    @Value.Parameter
    List<Item> items();

    @Value.Immutable(builder = false)
    interface Item {

        @Value.Parameter
        String fullEnclosingClassName();

        @Value.Parameter
        String displayName();

        @Value.Parameter
        String className();

        @Value.Parameter
        String methodName();
    }
}
