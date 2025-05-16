package com.varlanv.testkonvence.gradle.plugin;

import java.util.List;
import lombok.Value;

@Value
class APEnforcementMeta {

    List<Item> items;

    @Value
    public static class Item {

        String fullEnclosingClassName;
        String displayName;
        String className;
        String methodName;
    }
}
