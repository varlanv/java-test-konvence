package com.varlanv.testkonvence.gradle.plugin;

import lombok.Value;

import java.util.List;

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
